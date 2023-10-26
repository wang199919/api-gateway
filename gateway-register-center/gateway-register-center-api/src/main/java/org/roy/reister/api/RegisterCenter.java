package org.roy.reister.api;

import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;

/**
 * @author: roy
 * @date: 2023/10/26 10:41
 * @description:
 */
public interface RegisterCenter {

    /**
     * 初始化
     * @param registerAddress
     * @param env
     */
    void  init(String registerAddress,String env);

    /**
     * 注册
     * @param serviceDefinition
     * @param serviceInstance
     */
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 注销
     * @param serviceDefinition
     * @param serviceInstance
     */
    void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 订阅全部信息
     * @param registerCenterListener
     */
    void  subscribeAllServices(RegisterCenterListener registerCenterListener);

}
