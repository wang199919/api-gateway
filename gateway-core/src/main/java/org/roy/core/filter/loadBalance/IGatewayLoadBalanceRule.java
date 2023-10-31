package org.roy.core.filter.loadBalance;

import org.roy.common.config.ServiceInstance;
import org.roy.core.context.GatewayContext;

/**
 * @author: roy
 * @date: 2023/10/31 15:58
 * @description: 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {

    /**
     * 通过上下文获取服务实例
     * @param ctx
     * @return
     */
    ServiceInstance  choose(GatewayContext ctx);

    /**
     * 通过id获取服务实例
     * @param serviceID
     * @return
     */
    ServiceInstance choose(String serviceID)
        ;
}
