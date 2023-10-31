package org.roy.gateway.config.center.api;

/**
 * @author: roy
 * @date: 2023/10/29 13:02
 * @description:
 */
public interface ConfigCenter {

    void  init(String serverAddr,String env);

    void subscribeRulesChange(RulesChangeListener rulesChangeListener);

}
