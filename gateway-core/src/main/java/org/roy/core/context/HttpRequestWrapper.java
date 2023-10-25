package org.roy.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author: roy
 * @date: 2023/10/24 18:05
 * @description:
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest fullHttpRequest;
    private ChannelHandlerContext context;
}
