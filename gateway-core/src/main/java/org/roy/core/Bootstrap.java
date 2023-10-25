package org.roy.core;

/**
 * @author: roy
 * @date: 2023/10/22 10:35
 * @description:
 */
public class Bootstrap {
    public static void main(String[] args) {
        //加载核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config);
        //插件初始化
        //配置中心管理器连接配置中心,监听配置的增删改查
        //启动容器
        Container container=new Container(config);
        container.start();
        System.out.println(111111);
        //注册中心,实例加载到本地
        //服务优雅关机

    }
}
