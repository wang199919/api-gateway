package org.roy.gateway.config.center.api;

import org.roy.common.rules.Rule;

import java.util.List;

/**
 * @author: roy
 * @date: 2023/10/29 13:05
 * @description:
 */
public interface RulesChangeListener {

    void  onRulesChange(List<Rule> rules);
}
