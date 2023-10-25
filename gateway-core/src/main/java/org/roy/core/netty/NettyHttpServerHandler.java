package org.roy.core.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.roy.core.context.HttpRequestWrapper;
import org.roy.core.netty.processor.NettyProcessor;

/**
 * @author: roy
 * @date: 2023/10/24 11:28
 * @description:
 */
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private  final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest request= (FullHttpRequest) msg;

        HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
        httpRequestWrapper.setFullHttpRequest(request);
        httpRequestWrapper.setContext(ctx);
        nettyProcessor.process(httpRequestWrapper);
    }
}
