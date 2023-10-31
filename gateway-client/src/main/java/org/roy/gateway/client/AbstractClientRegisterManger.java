package org.roy.gateway.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;
import org.roy.gateway.client.core.ApiProperties;
import org.roy.gateway.client.core.ApiService;
import org.roy.reister.api.RegisterCenter;

import java.util.ServiceLoader;

/**
 * @author: roy
 * @date: 2023/10/26 16:33
 * @description:
 */
@Slf4j
public abstract class AbstractClientRegisterManger {
    @Getter
    private ApiProperties apiProperties;
    private RegisterCenter registerCenter;
    protected  AbstractClientRegisterManger(ApiProperties apiProperties){
        this.apiProperties=apiProperties;


        //初始化注册中心的对象
        ServiceLoader<RegisterCenter> serviceLoader=ServiceLoader.load(RegisterCenter.class);
        RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
                    log.info("not found RegisterCenter impl");
                    return new RuntimeException("not found");
                }
        );
    }

    protected  void  register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance){
        registerCenter.register(serviceDefinition,serviceInstance);
    }
    }

