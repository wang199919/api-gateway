package org.roy.gateway.client.core.autoconfigure;

import com.alibaba.dubbo.config.spring.ServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.roy.gateway.client.core.ApiProperties;
import org.roy.gateway.client.supprot.dubbe.DubboClientRegitsterManager;
import org.roy.gateway.client.supprot.springmvc.SpringMVCClientRegisterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * @author: roy
 * @date: 2023/10/29 12:36
 * @description: 自动装配主流程进行处理
 *
 *
 *
 * @ ConditionalOnClass
 * 这个注解可以用在Spring Boot的配置类或者Bean类上，用来控制是否创建这个类的Bean。如果指定的类不存在，那么相关的Bean将不会被创建。
 *
 * @ ConditionalOnMissingBean是一个Spring注解，
 * 用于指定当不存在指定类型的bean时，才会启用对应的配置。它常用于配置类中的@Bean方法上，用于控制Bean的创建条件
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "api",name = {"registerAddress"})
public class ApiClientAutoConfiguration {
    @Autowired
    private ApiProperties apiProperties;


    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegisterManager.class)
    public SpringMVCClientRegisterManager springMVCClientRegisterManager(){
        return new SpringMVCClientRegisterManager(apiProperties);
    }
    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(DubboClientRegitsterManager.class)
    public DubboClientRegitsterManager dubboClientRegitsterManager(){
        return new DubboClientRegitsterManager(apiProperties);
    }
}
