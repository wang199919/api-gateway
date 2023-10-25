package org.roy.core;

/**
 * @author: roy
 * @date: 2023/10/24 10:52
 * @description:
 */
public interface LifeCycle {
    /**
     * 初始化
     */
    void  init();

    /**
     * 启动!
     */
    void start();

    /**
     * 关闭
     */
    void  shutdown();
}
