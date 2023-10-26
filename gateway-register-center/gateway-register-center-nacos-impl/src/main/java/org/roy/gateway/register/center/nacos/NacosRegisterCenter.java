package org.roy.gateway.register.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;
import org.roy.common.constants.GatewayConst;

import org.roy.common.util.TimeUtil;
import org.roy.reister.api.RegisterCenter;
import org.roy.reister.api.RegisterCenterListener;

import javax.naming.Name;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: roy
 * @date: 2023/10/26 11:24
 * @description:
 */
@Slf4j
public class NacosRegisterCenter  implements RegisterCenter {

    private String registerAddress;

    private String env;
    //主要维护服务实例信息
    private NamingService namingService;

    //维护服务定义信息
    private NamingMaintainService namingMaintainSetvice;

    //监听器列表
    private List<RegisterCenterListener> registerCenterListeners;

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress=registerAddress;
        this.env=env;

        try {
            this.namingMaintainSetvice=NamingMaintainFactory.createMaintainService(registerAddress);
            this.namingService= NamingFactory.createNamingService(registerAddress);
        } catch (NacosException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        //构造nacos的实例信息
        Instance nacosInstance=new Instance();
        nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
        nacosInstance.setIp(serviceInstance.getIp());
        nacosInstance.setPort(serviceInstance.getPort());
        nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceInstance)));
        try {
            //注册
            namingService.registerInstance(serviceDefinition.getServiceId(),env,nacosInstance);
            //更新服务定义
            namingMaintainSetvice.updateService(serviceDefinition.getServiceId(),env,0,Map.of(GatewayConst.META_DATA_KEY,JSON.toJSONString(serviceDefinition)));
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.registerInstance(serviceDefinition.getServiceId(),env,serviceInstance.getIp(),serviceInstance.getPort());
        }catch (NacosException e){
            e.printStackTrace();
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener registerCenterListener) {
        registerCenterListeners.add(registerCenterListener);
        doSubscribeAllServices();

        //可能新服务加入,所以需要有一个定时任务来检查
        ScheduledExecutorService scheduledTheadPool = Executors.newScheduledThreadPool(1, new NameThreadFactory("doSubscribeAllServices"));
        scheduledTheadPool.scheduleWithFixedDelay(this::doSubscribeAllServices,10,10, TimeUnit.SECONDS);
    }

    private void doSubscribeAllServices() {
    try {
        //获取订阅的服务
        Set<String> subscribeService=namingService.getSubscribeServices().stream().map(ServiceInfo::getName).collect(Collectors.toSet());
        int pageNO=1;
        int pageSize=100;

        //nacos事件监听器
        EventListener eventListener=new NacosRegisterListener();
        //分页获取服务列表
        List<String> serviceDate = namingService.getServicesOfServer(pageNO, pageSize, env).getData();

        while(CollectionUtils.isEmpty(serviceDate)){
            log.info("service list size {}",serviceDate.size());
            for (String service: serviceDate) {
                if(subscribeService.contains(service)){
                    continue;
                }
                namingService.subscribe(service,eventListener);
                log.info("subscribe {} {}",service,env);
            }

            serviceDate=namingService.getServicesOfServer(++pageNO, pageSize, env).getData();

        }

    }catch (NacosException e){
        e.printStackTrace();
    }
    }

    private class NacosRegisterListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent){
                NamingEvent namingEvent= (NamingEvent) event;
                String serviceName=namingEvent.getServiceName();

                //获取服务定义的信息
                try {
                    Service service = namingMaintainSetvice.queryService(serviceName, env);
                    ServiceDefinition serviceDefinition = JSON.parseObject(service.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);

                    //获取服务定义信息
                    List<Instance> allInstances = namingService.getAllInstances(serviceName, env);
                    Set<ServiceInstance> serviceInstances=new HashSet<>();
                    for (Instance instance:allInstances ) {
                        ServiceInstance serviceInstance = JSON.parseObject(instance.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        serviceInstances.add(serviceInstance);
                    }
                    registerCenterListeners.stream().forEach(l-> l.onChange(serviceDefinition,serviceInstances));
                } catch (NacosException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
