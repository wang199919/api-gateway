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
import org.roy.core.filter.FilterFactory;
import org.roy.core.filter.GatewayFilterChainFactory;
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
    private FilterFactory filterFactory= GatewayFilterChainFactory.getInstance();
    @Override
    public void process(HttpRequestWrapper wrapper) {
        //1.定义接口
        FullHttpRequest fullHttpRequest = wrapper.getFullHttpRequest();
        ChannelHandlerContext context = wrapper.getContext();
        try {
            GatewayContext gatewayContext= RequestHelper.doContext(fullHttpRequest,context);
            filterFactory.buildFilterChain(gatewayContext).doFilter(gatewayContext);
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





}
