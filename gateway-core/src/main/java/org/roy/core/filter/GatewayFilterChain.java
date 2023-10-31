package org.roy.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.roy.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: roy
 * @date: 2023/10/30 14:04
 * @description: 过滤器链条
 */
@Slf4j
public class GatewayFilterChain {

    private List<Filter>  filters=null;

    public GatewayFilterChain addFilter(Filter filter){
        filters.add(filter);
        return  this;
    }

    public GatewayFilterChain addAllFilter(List<Filter> filter){
        filters.addAll(filter);
        return this;
    }

    public  GatewayContext doFilter(GatewayContext context)throws  Throwable{
        if (filters.isEmpty())return context;
        for (Filter f : filters) {
            try {
                f.doFilter(context);

            }catch (Exception e){
                log.info("执行过滤器发生异常,异常信息: {}",e.getMessage());
            }
        }
        return context;
    }


}
