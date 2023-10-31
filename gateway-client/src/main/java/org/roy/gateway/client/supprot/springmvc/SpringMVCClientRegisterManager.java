package org.roy.gateway.client.supprot.springmvc;

import afu.org.checkerframework.checker.oigj.qual.O;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.AprLifecycleListener;
import org.checkerframework.checker.units.qual.A;
import org.roy.common.config.ServiceDefinition;
import org.roy.common.config.ServiceInstance;
import org.roy.common.util.NetUtils;
import org.roy.common.util.TimeUtil;
import org.roy.gateway.client.AbstractClientRegisterManger;
import org.roy.gateway.client.core.ApiAnnotationScanner;
import org.roy.gateway.client.core.ApiProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static org.roy.common.constants.BasicConst.COLON_SEPARATOR;
import static org.roy.common.constants.GatewayConst.DEFAULT_WEIGHT;

/**
 * @author: roy
 * @date: 2023/10/27 20:16
 * @description:
 */
@Slf4j
public class SpringMVCClientRegisterManager extends AbstractClientRegisterManger implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {
    private  ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;


    private  Set<Object> set=new HashSet<>();



    public SpringMVCClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if (applicationEvent instanceof ApplicationStartedEvent){
            try {
                doRegisterSpringMvc();
            }catch (Exception e){
                e.printStackTrace();
            }

            log.info("springMvc api start");
        }
    }

    /**
     * 把服务注册到注册中心
     */
    private void doRegisterSpringMvc() {
        Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);
        for ( RequestMappingHandlerMapping handlerMapping:allRequestMappings.values()){
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
           // Set<Map.Entry<RequestMappingInfo, HandlerMethod>> entries = handlerMethods.entrySet();
            for (Map.Entry<RequestMappingInfo,HandlerMethod> me:handlerMethods.entrySet()){
                HandlerMethod handlerMethod=me.getValue();
                Class<?> clazz = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(clazz);
                if (set.contains(bean)){
                    continue;
                }
                ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);
                if (serviceDefinition==null){
                    continue;
                }
                serviceDefinition.setEnvType(getApiProperties().getEnv());

                //服务实
                ServiceInstance serviceInstance = new ServiceInstance();
                String LocalIp= NetUtils.getLocalIp();
                int port= serverProperties.getPort();

                String serviceInstenceID=LocalIp+COLON_SEPARATOR+port;
                String uniqueId=serviceDefinition.getUniqueId();
                String version=serviceDefinition.getVersion();

                serviceInstance.setServiceInstanceId(serviceInstenceID);
                serviceInstance.setIp(LocalIp);
                serviceInstance.setPort(port);
                serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                serviceInstance.setVersion(version);
                serviceInstance.setUniqueId(uniqueId);
                serviceInstance.setWeight(DEFAULT_WEIGHT);


                //注册
                register(serviceDefinition,serviceInstance);

            }
        }
    }
}
