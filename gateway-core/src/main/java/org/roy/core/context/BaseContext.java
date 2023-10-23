package org.roy.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author: roy
 * @date: 2023/10/22 11:23
 * @description:
 */
public class BaseContext implements IContext{
   //转发协议
    protected  final  String protocol;


    //状态 ,多线程情况下使用 volatile
    protected  volatile  int status=IContext.Running;

    //Netty上下文
    protected  final  ChannelHandlerContext nettyCtx;

    //异常
    protected  Throwable throwable;



 //是否保持长连接
    protected  final  boolean keepAlive;

    //回调函数集合
    protected List<Consumer<IContext>> completedCallBacks;

    //上下文参数
    protected  final Map<String,Object> attributes=new HashMap<>();

    protected  final AtomicBoolean requestReleased=new AtomicBoolean(false);


 public BaseContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
  this.protocol = protocol;
  this.nettyCtx = nettyCtx;
  this.keepAlive = keepAlive;
 }

 @Override
    public void runned() {
        status=IContext.Running;
    }

    @Override
    public void Writtened() {

  status=IContext.Written;
    }

    @Override
    public void completed() {
  status=IContext.Completed;
    }

    @Override
    public void terminated() {
  status=IContext.Terminated;
    }

    @Override
    public boolean isRunning() {
        return status==IContext.Running;
    }

    @Override
    public boolean isWrittened() {
        return status==IContext.Written;
    }

    @Override
    public boolean isCompleted() {
        return status==IContext.Completed;
    }

    @Override
    public boolean isTerminated() {
        return status==IContext.Terminated;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
  return throwable;
    }

    @Override
    public void setResponse(Object response) {

    }

    @Override
    public void setThrowable(Throwable throwable) {
  this.throwable=throwable;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return this.nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public boolean releaseRequest() {
        return false;
    }

    @Override
    public void setCompletedCallBack(Consumer<IContext> contextConsumer) {
  if(completedCallBacks==null){
   completedCallBacks=new ArrayList<>();
  }
  completedCallBacks.add(contextConsumer);
    }

    @Override
    public void invokeCompletedCallBack(Consumer<IContext> contextConsumer) {
if(completedCallBacks!=null){
 completedCallBacks.forEach(call-> call.accept(this));
}
    }
}
