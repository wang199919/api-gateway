package org.roy.core.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.roy.core.Config;
import org.roy.core.LifeCycle;
import org.roy.core.helper.AsyncHttpHelper;

import java.io.IOException;

/**
 * @author: roy
 * @date: 2023/10/25 12:09
 * @description:
 * 1.实现LifeCycle
 * 2.封装属性
 * 3.实现init 方法
 * 4. 实现start 方法
 * 5. 实现 shutdown方法
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {
    private  final Config config;

    private  final EventLoopGroup eventLoopGroupWoker;

    private AsyncHttpClient asyncHttpClient;
    public NettyHttpClient(Config config, EventLoopGroup eventLoopGroupWoker) {
        this.config = config;
        this.eventLoopGroupWoker = eventLoopGroupWoker;
        init();
    }

    @Override
    public void init() {
        DefaultAsyncHttpClientConfig build = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWoker)
                .setConnectTimeout(config.getHttpConnectTimeout())
                .setRequestTimeout(config.getHttpRequestTimeout())
                .setMaxRedirects(config.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(config.getHttpMaxConnections())
                .setMaxConnectionsPerHost(config.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeout())
                .build();
        this.asyncHttpClient=new DefaultAsyncHttpClient(build);
    }

    @Override
    public void start() {
        System.out.println("client启动");
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient!=null){
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {

                log.info("NettyHttpClient error {}",e);
                e.printStackTrace();
            }
        }
    }
}
