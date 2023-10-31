package org.roy.core.filter;

import java.lang.annotation.*;

/**
 * @author: roy
 * @date: 2023/10/30 13:57
 * @description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilterAspect {

    /**
     * 过滤器id
     * @return
     */
    String id();

    /**
     * 排序
     * @return
     */
    int order() default 0;

    String name() default "";
}
