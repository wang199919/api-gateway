package org.roy.gateway.client.core;

import java.lang.annotation.*;

/**
 * @author: roy
 * @date: 2023/10/26 15:45
 * @description: 在服务的方法上面强制申明
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    /**
     * 路径
     * @return
     */
    String path();
}
