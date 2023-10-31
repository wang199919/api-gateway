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


}
