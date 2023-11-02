package org.roy.core.filter.flowCtr;

import afu.org.checkerframework.checker.igj.qual.I;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.constants.FilterConst;
import org.roy.common.exception.NotFoundException;
import org.roy.common.rules.Rule;
import org.roy.core.context.GatewayContext;
import org.roy.core.redis.JedisUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.roy.common.constants.FilterConst.*;

/**
 * @author: roy
 * @date: 2023/11/4 11:16
 * @description:
 */
public class FlowCtlByPathRule implements IGatewayFlowCtlRule{


   private String path;
   private  String serviceID;
   private  static  final  String Limit_message="请求频繁,请稍后尝试";
   private  RedisCountLimiter redisCountLimiter;

    public FlowCtlByPathRule(String path, String serviceID, RedisCountLimiter redisCountLimiter) {
        this.path = path;
        this.serviceID = serviceID;
        this.redisCountLimiter = redisCountLimiter;
    }
    private static ConcurrentMap<String,FlowCtlByPathRule> serviceMap=new ConcurrentHashMap<>();

    public static  FlowCtlByPathRule getInstance(String path, String serviceID){
        StringBuffer buffer=new StringBuffer();
        String key=buffer.append(serviceID).append(".").append(path).toString();
        FlowCtlByPathRule flowCtlByPathRule=serviceMap.get(key);
        if (flowCtlByPathRule==null){
            flowCtlByPathRule=new FlowCtlByPathRule(path,serviceID,new RedisCountLimiter(new JedisUtil()));
            serviceMap.put(key,flowCtlByPathRule);
        }
        return flowCtlByPathRule;

    }

    /**
     * 根据路径执行限流
     * @param flowCtlConfig
     * @param serviceId
     */
    @Override
    public void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId) {
        if(flowCtlConfig==null|| StringUtils.isEmpty(serviceId)||StringUtils.isEmpty(flowCtlConfig.getConfig()))return;
        Map<String,Integer> configMap= JSON.parseObject(flowCtlConfig.getConfig(),Map.class);
        if (!configMap.containsKey(FLOW_CTL_LIMIT_DURATION)||!configMap.containsKey(FLOW_CTL_LIMIT_PERMITS)){
            return;
        }
        double duration=configMap.get(FLOW_CTL_LIMIT_DURATION);

        double permits=configMap.get(FLOW_CTL_LIMIT_PERMITS);

        boolean flag= true;
        if (FLOW_CTL_MODEL_PATH.equalsIgnoreCase(flowCtlConfig.getModel())){
            //todo 分布式限流
            StringBuffer buffer=new StringBuffer();
            String key=buffer.append(serviceID).append(".").append(path).toString();
            flag=redisCountLimiter.doFlowCtl(key,(int)permits,(int)duration);
        }else {
            GuavaCountLimiter guavaCountLimiter=GuavaCountLimiter.getInstance(serviceId,flowCtlConfig);
            if (guavaCountLimiter==null)throw  new RuntimeException("获取单机限流工具对象为空");
            double count=Math.ceil(permits/duration);
            flag=guavaCountLimiter.acquirer((int) count);
        }
        if(!flag){
            throw  new RuntimeException(Limit_message);
        }
    }
}
