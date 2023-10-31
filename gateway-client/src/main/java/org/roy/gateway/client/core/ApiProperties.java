package org.roy.gateway.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: roy
 * @date: 2023/10/27 20:01
 * @description: 环境与注册中心的地址
 */
@Data
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String registerAddress;

    private  String env="dev";


}
