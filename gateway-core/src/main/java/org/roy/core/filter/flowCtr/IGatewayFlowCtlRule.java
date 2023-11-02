package org.roy.core.filter.flowCtr;

import org.roy.common.rules.Rule;
import org.roy.core.context.GatewayContext;

/**
 * @author: roy
 * @date: 2023/11/4 11:10
 * @description: 执行网关接口
 */
public interface IGatewayFlowCtlRule {
    void  doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig,String serviceId);
}
