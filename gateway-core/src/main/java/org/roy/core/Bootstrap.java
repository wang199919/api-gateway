package org.roy.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.DynamicConfigManager;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;
import org.roy.common.util.JSONUtil;
import org.roy.common.util.NetUtils;
import org.roy.common.util.TimeUtil;
import org.roy.reister.api.RegisterCenter;
import org.roy.reister.api.RegisterCenterListener;

import java.util.Map;
import java.util.Set;

import static org.roy.common.constants.BasicConst.COLON_SEPARATOR;

/**
 * @author: roy
 * @date: 2023/10/22 10:35
 * @description:
 */
@Slf4j
public class Bootstrap {
    public static void main(String[] args) {
        //加载核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config);
        //插件初始化
        //配置中心管理器连接配置中心,监听配置的增删改查
        //启动容器
        Container container=new Container(config);
        container.start();
        System.out.println(111111);
        //注册中心,实例加载到本地
        final RegisterCenter registerCenter = registerAndSubscribe(config);
        //服务优雅关机
        //收到kill信息时调用
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config),buildGatewayServiceInstance(config));
            }
        });
    }

    private static RegisterCenter registerAndSubscribe(Config config) {
        final RegisterCenter registerCenter=null;
        //构造网关自定义与服务实例
        ServiceDefinition serviceDefinition= buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance=buildGatewayServiceInstance(config);
        //注册
        registerCenter.register(serviceDefinition,serviceInstance);
        //订阅
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstances) {
                log.info("refresh service and instence {} {}",serviceDefinition.getServiceId(), JSON.toJSON(serviceInstances));
                DynamicConfigManager manager=DynamicConfigManager.getInstance();
                manager.addServiceInstance(serviceDefinition.getUniqueId(),serviceInstance);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp= NetUtils.getLocalIp();
        int port=config.getPort();
        ServiceInstance serviceInstance=new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp+COLON_SEPARATOR+port );
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {

    ServiceDefinition serviceDefinition=new ServiceDefinition();
    serviceDefinition.setInvokerMap(Map.of());
    serviceDefinition.setUniqueId(config.getAplicationName());
    serviceDefinition.setServiceId(config.getAplicationName());
    serviceDefinition.setEnvType(config.getEnv());
    return serviceDefinition;
    }
}
