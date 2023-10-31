package org.roy.core.rounter;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.ConnectException;
import org.roy.common.exception.ResponseException;
import org.roy.core.ConfigLoader;
import org.roy.core.context.GatewayContext;
import org.roy.core.filter.Filter;
import org.roy.core.filter.FilterAspect;
import org.roy.core.helper.AsyncHttpHelper;
import org.roy.core.helper.ResponseHelper;
import org.roy.core.ressponse.GatewayResponse;

import java.io.File;
import java.util.Objects;
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
        Request request=gatewayContext.getRequest().build();
        System.out.println(request.getCookies());
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if(whenComplete){
            future.whenComplete(((response, throwable) -> {
                complete(request,response,throwable,gatewayContext);
            }));

        }else{
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request,response,throwable,gatewayContext);
            }));
        }
    }
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext) {
        gatewayContext.releaseRequest();
        System.out.println(throwable);
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
}
