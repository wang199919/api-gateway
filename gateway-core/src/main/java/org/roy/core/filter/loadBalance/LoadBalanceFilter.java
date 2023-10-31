package org.roy.core.filter.loadBalance;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.config.ServiceInstance;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.NotFoundException;
import org.roy.common.rules.Rule;
import org.roy.core.Config;
import org.roy.core.context.GatewayContext;
import org.roy.core.filter.Filter;
import org.roy.core.filter.FilterAspect;
import org.roy.core.request.GatewayRequest;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.roy.common.constants.FilterConst.*;

/**
 * @author: roy
 * @date: 2023/10/31 15:49
 * @description: 负载均衡过滤器
 */
@FilterAspect(id=LOAD_BALANCE_FILTER_ID,name = LOAD_BALANCE_FILTER_NAME,order = LOAD_BALANCE_FILTER_ORDER)
@Slf4j
public class LoadBalanceFilter  implements Filter {

    @Override
    public void doFilter(GatewayContext context) throws Exception {
        String serviceId=context.getUniqueId();
        IGatewayLoadBalanceRule gatewayLoadBalanceRule=getLoadBalanceRule(context);
        ServiceInstance serviceInstance=gatewayLoadBalanceRule.choose(context.getUniqueId());
        GatewayRequest request=context.getRequest();
        if (request!=null&&serviceInstance!=null){
            String host=serviceInstance.getIp()+":"+serviceInstance.getPort();
            request.setModifyHost(host);
        }else {
            log.warn("No instance available for{}",serviceId);
            throw  new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    private IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext context) {
        IGatewayLoadBalanceRule iGatewayLoadBalanceRule=null;
        Rule configRule=context.getRule();
        if (configRule!=null){
            Set<Rule.FilterConfig> filterConfigSet=configRule.getFilterConfigSet();
            Iterator iterator=filterConfigSet.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()){
                filterConfig= (Rule.FilterConfig) iterator.next();
                if(filterConfig==null)continue;
                String filterID=filterConfig.getId();
                if(filterID.equals(LOAD_BALANCE_FILTER_ID)){
                    String config=filterConfig.getConfig();
                    String strategy="";
                    if (StringUtils.isEmpty(config)){
                        Map<String,String> mapType= JSON.parseObject(config,Map.class);
                        strategy=mapType.get(LOAD_BALANCE_FILTER_KEY);

                    }
                    switch (strategy){
                        case  LOAD_BALANCE_RANDOM:
                            iGatewayLoadBalanceRule=new RandomLoadBalanceRule(context.getUniqueId());
                            break;
                        case LOAD_BALANCE_ROUND:
                            iGatewayLoadBalanceRule=new RoundRobinLoadBalanceRule(new AtomicInteger(1),context.getUniqueId());
                            break;
                    }
                }
            }
        }
        return iGatewayLoadBalanceRule;
    }
}
