package org.roy.core.filter.flowCtr;

import lombok.extern.slf4j.Slf4j;
import org.roy.common.rules.Rule;
import org.roy.core.context.GatewayContext;
import org.roy.core.filter.Filter;
import org.roy.core.filter.FilterAspect;

import java.util.Iterator;
import java.util.Set;

import static org.roy.common.constants.FilterConst.*;

/**
 * @Author: roy
 * @date: 2023/11/3 14:58
 * @description: 限流控制过滤器
 */
@Slf4j
@FilterAspect(id=FLOW_CTL_FILTER_ID,name = FLOW_CTL_FILTER_NAME,
order = FLOW_CTL_FILTER_ORDER)
public class FlowCtlFilter implements Filter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        Rule rule=context.getRule();
        if(rule!=null){
            Set<Rule.FlowCtlConfig> flowCtlConfigs = rule.getFlowCtlConfigs();

            Iterator iterator =flowCtlConfigs.iterator();
            Rule.FlowCtlConfig flowCtlConfig;
            while(iterator.hasNext()){
                flowCtlConfig= (Rule.FlowCtlConfig) iterator.next();
                IGatewayFlowCtlRule flowCtlRule=null;
                if (flowCtlConfig==null)continue;
                String path=context.getRequest().getPath();
                if (flowCtlConfig.getType().equalsIgnoreCase(FLOW_CTL_TYPE_PATH)&&path.equals(flowCtlConfig.getValue())){
                  flowCtlRule =new FlowCtlByPathRule(rule.getServiceId(),path);
                }else if(flowCtlConfig.getConfig().equalsIgnoreCase(FLOW_CTL_TYPE_SERVICE)){}
                if(flowCtlRule!=null){
                    flowCtlRule.doFlowCtlFilter(flowCtlConfig,rule.getServiceId());
                }
            }
        }
    }
}
