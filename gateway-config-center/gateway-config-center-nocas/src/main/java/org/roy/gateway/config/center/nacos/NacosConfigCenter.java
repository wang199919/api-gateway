package org.roy.gateway.config.center.nacos;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.NacosConfigService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.roy.common.rules.Rule;
import org.roy.gateway.config.center.api.ConfigCenter;
import org.roy.gateway.config.center.api.RulesChangeListener;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author: roy
 * @date: 2023/10/29 13:54
 * @description:
 */

@Slf4j
public class NacosConfigCenter implements ConfigCenter {

    private final  static String DATA_ID="api-gateway";

    private String serverAddr;
    private  String env;
    //配置中心交互
    private ConfigService configService;

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr=serverAddr;
        this.env=env;

        try {
            configService= NacosFactory.createConfigService(serverAddr);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribeRulesChange(RulesChangeListener rulesChangeListener) {
        try {
            //初始化通知
            //{"rules":[{rule},{}] }
            String config = configService.getConfig(DATA_ID, env, 5000);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            rulesChangeListener.onRulesChange(rules);

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String s) {
                    List<Rule> rules1=JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
                }
            });

        } catch (NacosException e) {
            e.printStackTrace();
        }

    }
}
