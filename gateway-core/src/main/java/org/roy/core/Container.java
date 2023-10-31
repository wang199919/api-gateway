package org.roy.core;

import lombok.extern.slf4j.Slf4j;
import org.roy.core.netty.NettyHttpClient;
import org.roy.core.netty.NettyHttpServer;
import org.roy.core.netty.processor.NettyProcessor;
import org.roy.core.netty.processor.NettyCoreProcessor;

/**
 * @author: roy
 * @date: 2023/10/25 12:07
 * @description: 进行启动 容器进行监视数据库进行处理
 *
 */
@Slf4j
public class Container implements LifeCycle{
    private  final  Config config;

    private NettyHttpServer nettyHttpServer;
    private NettyHttpClient nettyHttpClient;
    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
     this.nettyProcessor=new NettyCoreProcessor();

     this.nettyHttpServer=new NettyHttpServer(config,nettyProcessor);
     this.nettyHttpClient=new NettyHttpClient(config,nettyHttpServer.getEventExecutorsWorker());
    }

    @Override
    public void start() {
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("api gateway started");
        System.out.println("api gateway started");
    }

    @Override
    public void shutdown() {
        nettyHttpClient.shutdown();
        nettyHttpServer.shutdown();
        log.info("api gateway shutdown");
        System.out.println("api gateway shutdown" );
    }
}
