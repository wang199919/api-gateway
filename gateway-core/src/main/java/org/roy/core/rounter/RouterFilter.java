package org.roy.core.rounter;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.netflix.hystrix.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.ConnectException;
import org.roy.common.exception.ResponseException;
import org.roy.common.rules.Rule;
import org.roy.core.ConfigLoader;
import org.roy.core.context.GatewayContext;
import org.roy.core.filter.Filter;
import org.roy.core.filter.FilterAspect;
import org.roy.core.helper.AsyncHttpHelper;
import org.roy.core.helper.ResponseHelper;
import org.roy.core.ressponse.GatewayResponse;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.roy.common.constants.FilterConst.*;

/**
 * @author: roy
 * @date: 2023/10/31 19:23
 * @description:
 */
@Slf4j
@FilterAspect(id=ROUTER_FILTER_ID,
order = ROUTER_FILTER_ORDER,
name = ROUTER_FILTER_NAME)
public class RouterFilter implements Filter {
    @Override
    public void doFilter(GatewayContext gatewayContext) throws Exception {

        Optional<Rule.HystrixConfig> hystrixConfig=getHystixConfig(gatewayContext);
        if (hystrixConfig.isPresent()){
            rounteWithHystrixConfig(gatewayContext,hystrixConfig);

        }else{
            rounte(gatewayContext,hystrixConfig);
        }

    }

    private void rounteWithHystrixConfig(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        HystrixCommand.Setter setter=HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory
                .asKey(gatewayContext.getUniqueId()))
                .andCommandKey(HystrixCommandKey.Factory
                        .asKey(gatewayContext.getRequest().getPath()))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(hystrixConfig.get().getThreadCoreSize()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(hystrixConfig.get().getTimeoutInMillinseconds())
                        .withExecutionTimeoutEnabled(true)
                );
        new HystrixCommand<Object >(setter){
            @Override
            protected Object run() throws Exception {
                rounte(gatewayContext,hystrixConfig).get();
                return null;
            }

            @Override
            protected Object getFallback() {
                gatewayContext.setResponse(hystrixConfig);
                gatewayContext.writtened();
                return  null;
            }
        }.execute();
    }

    private CompletableFuture<Response> rounte(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        Request request=gatewayContext.getRequest().build();
        System.out.println(request.getCookies());
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if(whenComplete){
            future.whenComplete(((response, throwable) -> {
                complete(request,response,throwable,gatewayContext,hystrixConfig);
            }));

        }else{
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request,response,throwable,gatewayContext,hystrixConfig);
            }));
        }
        return  future;
    }

    private Optional<Rule.HystrixConfig> getHystixConfig(GatewayContext gatewayContext) {
        Rule rule=gatewayContext.getRule();
        Optional<Rule.HystrixConfig> hystrixConfig=rule.getHystrixConfigs().stream().filter(c-> StringUtils.equals(c.getPath(),gatewayContext.getRequest().getPath())).findFirst();
        return  hystrixConfig;
    }

    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext,Optional<Rule.HystrixConfig> hystrixConfig) {
        gatewayContext.releaseRequest();
        System.out.println(throwable);
        int currentRetryTimes=gatewayContext.getCurrentRetryTimes();
        int conRetryTimes=gatewayContext.getRule().getRetry();
        if (throwable instanceof TimeoutException||throwable instanceof IOException&&currentRetryTimes<=conRetryTimes&&hystrixConfig.isPresent()){
        doRetry(currentRetryTimes,gatewayContext);
        }
        try {
            if( Objects.nonNull(throwable)){
                String url=request.getUrl();
                System.out.println(url);
                if (throwable instanceof TimeoutException){
                    log.warn("complete time out {}",url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                }else {
                    gatewayContext.setThrowable(new ConnectException(throwable,gatewayContext.getUniqueId(),url,ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }else {
                gatewayContext.setResponse(GatewayResponse.BuildGatewayResponse(response));
            }

        }catch (Throwable t){
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error",t);
        }finally {
            gatewayContext.writtened();
            ResponseHelper.writeResponse(gatewayContext);
        }

    }

    private void doRetry(int currentRetryTimes, GatewayContext gatewayContext) {
        gatewayContext.setCurrentRetryTimes(currentRetryTimes+1);
        try {
            doFilter(gatewayContext);
        } catch (Exception e) {

        }

    }
}
