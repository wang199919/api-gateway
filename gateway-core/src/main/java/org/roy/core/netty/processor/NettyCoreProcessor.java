package org.roy.core.netty.processor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.BaseException;
import org.roy.common.exception.ConnectException;
import org.roy.common.exception.ResponseException;
import org.roy.core.ConfigLoader;
import org.roy.core.context.GatewayContext;
import org.roy.core.context.HttpRequestWrapper;
import org.roy.core.helper.AsyncHttpHelper;
import org.roy.core.helper.RequestHelper;
import org.roy.core.helper.ResponseHelper;
import org.roy.core.ressponse.GatewayResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author: roy
 * @date: 2023/10/24 18:18
 * @description:
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {
    @Override
    public void process(HttpRequestWrapper wrapper) {
        //1.定义接口
        FullHttpRequest fullHttpRequest = wrapper.getFullHttpRequest();
        ChannelHandlerContext context = wrapper.getContext();
        try {
            GatewayContext gatewayContext= RequestHelper.doContext(fullHttpRequest,context);
            System.out.println(gatewayContext.getRequest().getHttpHeader());
            try {
                System.out.println(gatewayContext.request.getCookieALL());
            }catch (Exception e){
                System.out.println(e);
            }

            route(gatewayContext);
        }catch (BaseException e){
            log.error("process error {} {}",e.getCode().getCode(),e.getCode().getCode());
            FullHttpResponse response=ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(context,fullHttpRequest,response);
        }catch (Throwable t){
            log.error("process unkown error",t);
            FullHttpResponse httpResponse=ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(context,fullHttpRequest,httpResponse);
        }

        //2.最小可用核心
        //3.路由方法实现
        //4.获取异步配置
        //5.异常处理
        //6.写回响应信息,并释放资源
    }

    private void doWriteAndRelease(ChannelHandlerContext context, FullHttpRequest fullHttpRequest, FullHttpResponse response) {
        context.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);//释放资源关闭channel\
        ReferenceCountUtil.release(fullHttpRequest);
    }

    private void route(GatewayContext gatewayContext) {
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
