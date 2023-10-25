package org.roy.core;

import lombok.Data;

import javax.naming.Name;

/**
 * @author: roy
 * @date: 2023/10/23 15:05
 * @description:
 */
@Data
public class Config {
    //基本配置
    private  int port=8888;
    private String aplicationName="api-gateway";
    private String registryAddress="127.0.0.1:8848";
    private String env="evn";

    //netty配置
    //Boss线程池线程数
    private  int eventLoopGroupBossNum=1;
    //工作线程数,一个CPU核心数
    private  int eventLoopGroupWorkNum=Runtime.getRuntime().availableProcessors();

    //Http报文限制
    private  int maxContentLength=64*1024;

    //默认单异步
    private  boolean whenComplete;
    //	连接超时时间
    private int httpConnectTimeout = 30 * 1000;

    //	请求超时时间
    private int httpRequestTimeout = 30 * 1000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

}
