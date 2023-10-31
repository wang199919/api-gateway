package org.roy.core.filter;

import org.roy.core.context.GatewayContext;

/**
 * @author: roy
 * @date: 2023/10/30 14:00
 * @description: 工厂接口
 */
public interface FilterFactory {
    /**
     * 构建过滤器链条
     * @param context
     * @return
     * @throws Exception
     */
    GatewayFilterChain buildFilterChain(GatewayContext context) throws  Exception;

    /**
     *
     * 通过id 获取过滤器
     * @param filterId
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getFilterInfo(String filterId) throws  Exception;

}
