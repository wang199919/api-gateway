package org.roy.reister.api;

import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;

import java.util.Set;

/**
 * @author: roy
 * @date: 2023/10/26 10:46
 * @description:
 */
public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstances);
}
