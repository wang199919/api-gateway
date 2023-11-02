package org.roy.core.filter.flowCtr;

import lombok.extern.slf4j.Slf4j;
import org.roy.core.filter.FilterAspect;
import org.roy.core.redis.JedisUtil;

/**
 * @author: roy
 * @date: 2023/11/4 13:31
 * @description: 使用Redis 实现分布式限流
 */
@Slf4j
public class RedisCountLimiter {
    protected JedisUtil jedisUtil;

    public RedisCountLimiter(JedisUtil jedisUtil) {
        this.jedisUtil = jedisUtil;
    }

    private  static  final   int SUCCESS_RESULT=1;
    private  static  final  int FAILED_RESULT=0;

    /**
     * 执行线程
     * @param key
     * @param limit
     * @param expire
     * @return
     */
    public  boolean doFlowCtl(String key,int limit, int expire){
        try {
            Object o=jedisUtil.executeScript(key,limit,expire);
            if (o==null)return  true;
            Long result=Long.valueOf(o.toString());
            return FAILED_RESULT != result;
        }catch (Exception e){
            throw  new RuntimeException("分布式限流失败");
        }
    }
}
