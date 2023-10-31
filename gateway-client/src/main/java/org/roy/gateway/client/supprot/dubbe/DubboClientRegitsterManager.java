package org.roy.gateway.client.supprot.dubbe;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import lombok.extern.slf4j.Slf4j;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;
import org.roy.common.util.NetUtils;
import org.roy.common.util.TimeUtil;
import org.roy.gateway.client.AbstractClientRegisterManger;
import org.roy.gateway.client.core.ApiAnnotationScanner;
import org.roy.gateway.client.core.ApiProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

import static org.roy.common.constants.BasicConst.COLON_SEPARATOR;
import static org.roy.common.constants.GatewayConst.DEFAULT_WEIGHT;

/**
 * @author: roy
 * @date: 2023/10/29 12:14
 * @description:
 */
@Slf4j
public class DubboClientRegitsterManager  extends AbstractClientRegisterManger implements ApplicationListener<ApplicationEvent> {

    private Set<Object> set=new HashSet<>();

    public DubboClientRegitsterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
         if (applicationEvent instanceof ServiceBeanExportedEvent){
             try {
                 ServiceBean serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();
                 doRegisterDubbo(serviceBean);
             }catch (Exception e){
                 log.info("doRegisterDubbo error",e);
             }
         }else if (applicationEvent instanceof ApplicationStartedEvent) {
             log.info("dubbo api start");
         }
    }

    private void doRegisterDubbo(ServiceBean serviceBean) {
        Object bean=serviceBean.getRef();

        if (set.contains(bean))return;

        ServiceDefinition definition = ApiAnnotationScanner.getInstance().scanner(bean, serviceBean);

        if (definition==null){
            return;
        }
        definition.setEnvType(getApiProperties().getEnv());
        ServiceInstance serviceInstance = new ServiceInstance();
        String LocalIp= NetUtils.getLocalIp();
        int port= serviceBean.getProtocol().getPort();

        String serviceInstenceID=LocalIp+COLON_SEPARATOR+port;
        String uniqueId=definition.getUniqueId();
        String version=definition.getVersion();

        serviceInstance.setServiceInstanceId(serviceInstenceID);
        serviceInstance.setIp(LocalIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        serviceInstance.setVersion(version);
        serviceInstance.setUniqueId(uniqueId);
        serviceInstance.setWeight(DEFAULT_WEIGHT);

        register(definition,serviceInstance);
    }
}
