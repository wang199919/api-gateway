package org.roy.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * @author: roy
 * @date: 2023/10/22 10:58
 * @description:
 */
public interface IContext {
    /*
    * 生命周期状态
    * 运行中 : 1
    * */
    int Running =1;

    /*
    * 运行过程中发送错误, 对齐进行标记,告诉我们请求已经结束,需要返回客户端
    * */
    int Written =0;

    /*
    * 标记写回成功,防止并发重复写回
    * */
    int Completed=1;


    /*
    * 网关请求结束
    * */
    int Terminated=2;

    /*
    * 设置上下文状态为运行中
    * */
    void runned();

    /*
    * 设置上下文为标记写回
    * */
    void writtened();

    /*
    * 设置上下文状态为写回结束
    * */
    void completed();

    /*
    * 设置上下文为请求结束
    * */
    void  terminated();

    /*
    * 判断网关状态
    * */
    boolean isRunning();
    boolean isWrittened();
    boolean isCompleted();
    boolean isTerminated();

    /*
    * 获取协议
    * */
    String  getProtocol();

    /*
    * 获取返回对象
    * */
    Object getRequest();

    Object getResponse();

    Throwable getThrowable();

    /*
    * 设置返回对象
    * */
    void  setResponse(Object response);

    /*
    * 设置
    * */
    void setThrowable(Throwable throwable);


    /*
    * 获取Netty上下文
    * */
    ChannelHandlerContext getNettyCtx();

    /*
    * 是否保持连接
    * */
    boolean isKeepAlive();

    /*
    * 释放请求资源
    * */
    boolean releaseRequest();

    /*
    * 设置写回接收回调函数
    * */
    void  setCompletedCallBack(Consumer <IContext> contextConsumer);

    /*
    * 执行函数
    * */
    void  invokeCompletedCallBack();

}
