package org.roy.gateway.client.core;

import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.config.*;
import org.roy.common.constants.BasicConst;
import org.roy.gateway.client.supprot.dubbe.DubboConstants;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: roy
 * @date: 2023/10/26 15:50
 * @description: 注解扫描类
 */
public class ApiAnnotationScanner {

    /**
     * 单例模式建立
     */
    private ApiAnnotationScanner() {
    }

    private static  class SingletonHolder{
        static  final ApiAnnotationScanner INSTANSE=new ApiAnnotationScanner();
    }

    public static ApiAnnotationScanner getInstance(){
        return SingletonHolder.INSTANSE;
    }


    /**
     * 扫描传入的bean对象,最终返回一个服务对象
     * @param bean
     * @param args
     * @return
     */
    public ServiceDefinition scanner(Object bean,Object... args){
        Class<?> aClass = bean.getClass();
        if(aClass.isAnnotationPresent(ApiService.class)){
            return  null;
        }
        ApiService apiService = aClass.getAnnotation(ApiService.class);
        String serviceID=apiService.serviceId();
        ApiProtocol protocol=apiService.protocol();
        String pattrenPath=apiService.patterPath();
        String version =apiService.version();
         ServiceDefinition definition=new ServiceDefinition();
        Map<String, ServiceInvoker> invokerMap=new HashMap<>();
        Method[] methods = aClass.getMethods();
        if (methods!=null&&methods.length>0) {
            for (Method method : methods) {
                ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
                if (apiInvoker == null) {
                    continue;
                }
                String path = apiInvoker.path();
                switch (protocol) {
                    case HTTP:
                        HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                        invokerMap.put(path, httpServiceInvoker);
                        break;
                    case DUBBD:
                        ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                        DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                        String dubboVersion = dubboServiceInvoker.getVersion();
                        if (!StringUtils.isBlank(dubboVersion)) {
                            version = dubboVersion;
                        }
                        invokerMap.put(path, dubboServiceInvoker);
                        break;
                    default:
                        break;
                }
            }

        }
        definition.setUniqueId(serviceID+ BasicConst.COLON_SEPARATOR+version);
        definition.setServiceId(serviceID);
        definition.setVersion(version);
        definition.setProtocol(protocol.getCode());
        definition.setPatternPath(pattrenPath);
        definition.setEnable(true);
        definition.setInvokerMap(invokerMap);
        return definition;
    }


    /**
     * 构建httpServiceInvoker 对象
     * @param path
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path){
        HttpServiceInvoker httpServiceInvoker=new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }
    /**
     * 构建DubboServiceInvoker对象
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);

        String methodName = method.getName();
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();

        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setInterfaceClass(interfaceClass);

        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);

        Integer seriveTimeout = serviceBean.getTimeout();
        if (seriveTimeout == null || seriveTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if (providerTimeout == null || providerTimeout.intValue() == 0) {
                    seriveTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    seriveTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(seriveTimeout);

        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);

        return dubboServiceInvoker;
    }
}
