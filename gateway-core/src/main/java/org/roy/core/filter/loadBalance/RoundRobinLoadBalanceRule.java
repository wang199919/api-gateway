package org.roy.core.filter.loadBalance;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.DynamicConfigManager;
import org.roy.common.config.ServiceInstance;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.NotFoundException;
import org.roy.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: roy
 * @date: 2023/10/31 16:10
 * @description: 负载均衡- 轮询算法
 */
@Slf4j
public class RoundRobinLoadBalanceRule implements  IGatewayLoadBalanceRule{

    //轮询位置
    final AtomicInteger atomicInteger;

    private final  String serviveId;

    private Set<ServiceInstance> serviceInstanceSet;

    public RoundRobinLoadBalanceRule(AtomicInteger atomicInteger, String serviveId) {
        this.atomicInteger = atomicInteger;
        this.serviveId = serviveId;
        serviceInstanceSet= DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviveId);
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        String serviceId=ctx.getUniqueId();
        return choose(serviceId);
    }

    @Override
    public ServiceInstance choose(String serviceID) {
        if (serviceInstanceSet.isEmpty())serviceInstanceSet=DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceID);

        if (serviceInstanceSet.isEmpty()) {log.warn("NO instance available for:{}", serviceID);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> instances =new ArrayList<ServiceInstance>(serviceInstanceSet);
        if (instances.isEmpty()){
            log.warn("No instance available for service:{}",serviceID);
            return  null;
        }else {
            int pos= Math.abs(this.atomicInteger.incrementAndGet());
            return instances.get(pos%instances.size());
        }
    }
}
