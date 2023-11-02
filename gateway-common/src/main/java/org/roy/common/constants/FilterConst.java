package org.roy.common.constants;

/**
 * @author: roy
 * @date: 2023/10/31 15:50
 * @description: 负载均衡常量类
 */
public interface FilterConst {
  String  LOAD_BALANCE_FILTER_ID="load_balancer_filter";
    String  LOAD_BALANCE_FILTER_NAME="load_balancer_filter";
    int  LOAD_BALANCE_FILTER_ORDER =100;




    String LOAD_BALANCE_FILTER_KEY="load_balancer";
    String LOAD_BALANCE_RANDOM="random";
    String LOAD_BALANCE_ROUND="RoundRobin";

    String ROUTER_FILTER_ID="router_filter";
    String ROUTER_FILTER_NAME="router_filter";
    int ROUTER_FILTER_ORDER =Integer.MAX_VALUE;


    String  FLOW_CTL_FILTER_ID="flow_ctl_filter";
    String  FLOW_CTL_FILTER_NAME="flow_ctl_filter";
    int  FLOW_CTL_FILTER_ORDER =50;

    String FLOW_CTL_TYPE_PATH="path";
    String FLOW_CTL_TYPE_SERVICE="service";

    String FLOW_CTL_LIMIT_DURATION="duration";
    String FLOW_CTL_LIMIT_PERMITS="permits";

    String FLOW_CTL_MODEL_PATH="distributed";
    String FLOW_CTL_MODEL_SINGLETON="Singlenton";


}
