package org.roy.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Value;
import org.roy.common.rules.Rule;
import org.roy.common.util.AssertUtil;
import org.roy.core.ressponse.GatewayResponse;
import org.roy.core.request.GatewayRequest;

/**
 * @author: roy
 * @date: 2023/10/22 12:46
 * @description: 网关上下文
 */
public class GatewayContext extends  BaseContext {

    public GatewayRequest request;
    public GatewayResponse response;
    public Rule rule;

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive, GatewayRequest request, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
    }

    public  static  class Builder{
        private  String protocol;
        private  ChannelHandlerContext nettyCtx;
        private GatewayRequest gatewayRequest;
        private Rule rule;
        private boolean keepAlive;


        public  Builder(){

        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setGatewayRequest(GatewayRequest gatewayRequest) {
            this.gatewayRequest = gatewayRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public  GatewayContext build(){
            AssertUtil.notEmpty(protocol,"protocol不能为空");
            AssertUtil.notNull(nettyCtx,"nettyCtx不能为空");
            AssertUtil.notNull(gatewayRequest,"gatewayRequest不能为空");
            AssertUtil.notNull(rule,"rule不能为空");
            return  new GatewayContext(protocol,nettyCtx,keepAlive,gatewayRequest,rule);
        }
    }

    /**
     * 获取Key的上下文参数
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getRequireAttribute(String key){
        T value=getRequireAttribute(key);
        AssertUtil.notNull(value,"缺少必要常数");
        return  value;
    }

    /**
     * 根据key获取上下文,如果没有, 则默认
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T getRequireAttribute(String key,T defaultValue){
        return (T) attributes.getOrDefault(key,defaultValue);
    }

    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfigById(filterId);
    }

    /**
     * 获取服务ID
     * @return
     */
    public  String getUniqueId(){
        return request.getUniqueId();
    }

    /**
     * 重写父类释放资源,
      */
    public  boolean releaseRequest(){
        if (requestReleased.compareAndSet(false,true)){
            ReferenceCountUtil.release(request.getFullHttpRequest());
            return  true;
        }
        return  false;
    }

    /**
     * 获取原始请求对象
     * @return
     */
    public  GatewayRequest getOriginRequest(){
        return  request;
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }
    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    public void setResponse(Object response) {
        this.response = (GatewayResponse) response;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
