package org.roy.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import org.roy.common.util.RemotingUtil;
import org.roy.core.Config;
import org.roy.core.LifeCycle;
import org.roy.core.netty.processor.NettyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author: roy
 * @date: 2023/10/24 11:00
 * @description:
 */
public class NettyHttpServer implements LifeCycle {

    private  static  final Logger log= LoggerFactory.getLogger(NettyHttpServer.class);
    //1.封装属性
    private  final Config config;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventExecutorsBoss;
    @Getter
    private  EventLoopGroup eventExecutorsWorker;
    private  final NettyProcessor processor ;
    //2.构造方法
    public NettyHttpServer(Config config,NettyProcessor processor) {
        this.config = config;

        this.processor=processor;
        this.init();
    }
    //3.实现init方法
    @Override
    public void init() {
        //4.epoll优化
        if(useEpoll()){
            this.serverBootstrap=new ServerBootstrap();
            this.eventExecutorsBoss=new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),new DefaultThreadFactory("netty-boss-nio"));
            this.eventExecutorsWorker=new EpollEventLoopGroup(config.getEventLoopGroupWorkNum(),new DefaultThreadFactory("netty-work-nio"));
        }else{
        this.serverBootstrap=new ServerBootstrap();
        this.eventExecutorsBoss=new NioEventLoopGroup(config.getEventLoopGroupBossNum(),new DefaultThreadFactory("netty-boss-nio"));
        this.eventExecutorsWorker=new NioEventLoopGroup(config.getEventLoopGroupWorkNum(),new DefaultThreadFactory("netty-work-nio"));
    }}

    public  boolean useEpoll(){
        return RemotingUtil.isLinuxPlatform()&& Epoll.isAvailable();
    }
    //5.实现start方法
    @Override
    public void start() {
        System.out.println("server 启动");
        this.serverBootstrap
                .group(eventExecutorsBoss,eventExecutorsWorker)
                .channel(useEpoll()? EpollServerDomainSocketChannel.class: NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                   channel.pipeline().addLast(
                           new HttpServerCodec(), //http解码器
                           new HttpObjectAggregator(config.getMaxContentLength()),//
                           new NettyServerConnectManagerHandler(),
                           new NettyHttpServerHandler(processor)
                   );
                    }
                });
        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}",config.getPort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //6.实现shutdown方法
    @Override
    public void shutdown() {
        if (eventExecutorsBoss!=null){
            eventExecutorsBoss.shutdownGracefully();
        }
        if(eventExecutorsWorker!=null){
            eventExecutorsWorker.shutdownGracefully();
        }
    }


}
