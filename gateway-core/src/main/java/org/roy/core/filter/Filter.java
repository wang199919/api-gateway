package org.roy.core.filter;

import org.roy.core.context.GatewayContext;

/**
 * @author: roy
 * @date: 2023/10/30 13:55
 * @description: 过滤器顶级接口
 */
public interface Filter {

    void  doFilter(GatewayContext context) throws Exception;

    default int getOrder() {
        FilterAspect annotation=this.getClass().getAnnotation(FilterAspect.class);
        if(annotation!=null){
            return  annotation.order();
        }
        return Integer.MAX_VALUE;
    }
}
