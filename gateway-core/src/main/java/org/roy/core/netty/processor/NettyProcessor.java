package org.roy.core.netty.processor;

import io.netty.channel.ChannelInboundHandlerAdapter;
import org.roy.core.context.HttpRequestWrapper;

/**
 * @author: roy
 * @date: 2023/10/24 18:06
 * @description: 核心处理器
 */
public interface NettyProcessor  {
    //1.定义接口
    //2.最小可用核心
    //3.路由方法实现
    //4.获取异步配置
    //5.异常处理
    //6.写回响应信息,并释放资源
    void  process(HttpRequestWrapper wrapper);

}
