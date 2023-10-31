package org.roy.core.filter.loadBalance;

import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.DynamicConfigManager;
import org.roy.common.config.ServiceInstance;
import org.roy.common.enums.ResponseCode;
import org.roy.common.exception.NotFoundException;
import org.roy.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: roy
 * @date: 2023/10/31 16:01
 * @description: 负载均衡- 随机算法
 */
@Slf4j
public class RandomLoadBalanceRule implements  IGatewayLoadBalanceRule{

    private final  String serviveId;

    private Set<ServiceInstance> serviceInstanceSet;


    public RandomLoadBalanceRule(String serviveId) {
        this.serviveId = serviveId;
        this.serviceInstanceSet=DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviveId);
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        String serviceId=ctx.getUniqueId();
        return choose(serviceId);
    }

    @Override
    public ServiceInstance choose(String serviceID) {
        //加载延时
        if (serviceInstanceSet.isEmpty())
            serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceID);

        if (serviceInstanceSet.isEmpty()) {log.warn("NO instance available for:{}", serviceID);
        throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> instances=new ArrayList<ServiceInstance>(serviceInstanceSet);
        int index= ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance instance=(ServiceInstance) instances.get(index);
        return  instance;
    }
}
