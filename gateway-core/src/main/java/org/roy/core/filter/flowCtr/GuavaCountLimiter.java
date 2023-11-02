package org.roy.core.filter.flowCtr;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.rules.Rule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: roy
 * @date: 2023/11/4 11:42
 * @description: 单机实现限流
 */
public class GuavaCountLimiter {



    private RateLimiter rateLimiter;

    private  double maxPermits;

    public GuavaCountLimiter( double maxPermits) {
        this.rateLimiter = RateLimiter.create(maxPermits);
        this.maxPermits = maxPermits;
    }


    public GuavaCountLimiter(long warmUpPeriodAsSecond, double maxPermits) {
        this.maxPermits = maxPermits;
        rateLimiter=RateLimiter.create(maxPermits,warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }

    public  static ConcurrentMap<String,GuavaCountLimiter> limiterConcurrentMap=new ConcurrentHashMap<>();
    public  static  GuavaCountLimiter getInstance(String serviceID, Rule.FlowCtlConfig flowCtlConfig){
        if (StringUtils.isEmpty(serviceID)||StringUtils.isEmpty(flowCtlConfig.getConfig())||StringUtils.isEmpty(flowCtlConfig.getValue())||StringUtils.isEmpty(flowCtlConfig.getType()))return null;
        StringBuffer  stringbuffer=new StringBuffer();
        String key=stringbuffer.append(serviceID).append(".").append(flowCtlConfig.getValue()).toString();
        GuavaCountLimiter limiter=limiterConcurrentMap.get(key);
        if (limiter==null){
            limiter=new GuavaCountLimiter(50);
            limiterConcurrentMap.put(key,limiter);
        }
        return  limiter;
    }

    public  boolean acquirer(int permits){
        //进行尝试
        boolean success=rateLimiter.tryAcquire(permits);
        if (success) {
            return true;
        }
        return  false;
    }
}
