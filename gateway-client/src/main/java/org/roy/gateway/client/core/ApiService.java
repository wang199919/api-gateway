package org.roy.gateway.client.core;

/**
 * @author: roy
 * @date: 2023/10/26 15:36
 * @description: 服务定义
 */

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {
    /**
     * 服务ID
     * @return
     */
    String  serviceId();

    /**
     * 服务版本号,默认为1.0.0
     * @return
     */
    String version() default "1.0.0";

    /**
     * 服务协议 http 或者 dubbe
     * @return
     */
    ApiProtocol protocol();

    /**
     * 服务的请求路径
     * @return
     */
    String patterPath();
}
