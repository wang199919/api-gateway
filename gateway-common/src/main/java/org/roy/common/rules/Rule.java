package org.roy.common.rules;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

/**
 * @author: roy
 * @date: 2023/10/23 10:51
 * @description:
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {

    //全局唯一id
    private String id;

    //名称
    private  String name;

    //协议
    private String protocol;

    //规则优先级
    private  Integer order;


    //服务id
    private  String serviceId;

    //请求前缀
    private  String prefix;

    private List<String> path;
    //过滤器规则
    private Set<FilterConfig> filterConfigSet=new HashSet<>();

    //限流规则
    private  Set<FlowCtlConfig> flowCtlConfigs=new HashSet<>();
    private  RetryConfig retryConfig=new RetryConfig();
    private  Set<HystrixConfig> hystrixConfigs=new HashSet<>();
    public Rule() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id) && Objects.equals(name, rule.name) && Objects.equals(protocol, rule.protocol) && Objects.equals(order, rule.order) && Objects.equals(filterConfigSet, rule.filterConfigSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, protocol, order, filterConfigSet);
    }

    public Rule(String id, String name, String protocol, Integer order, String serviceId, String prefix, List<String> path, Set<FilterConfig> filterConfigSet) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.order = order;
        this.serviceId = serviceId;
        this.prefix = prefix;
        this.path = path;
        this.filterConfigSet = filterConfigSet;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare=Integer.compare(getOrder(),o.getOrder());
        if(orderCompare==0){
            return  getId().compareTo(o.id);
        }
        return orderCompare;
    }

    public static class FilterConfig {
    //规则配置id
    private  String id;
        /**
         * 配置信息
         */
    private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public boolean equals(Object o){
            if (this ==o) {
                return true;
            }
            if (o==null||getClass()!=o.getClass()){
                return false;
            }

            FilterConfig that = (FilterConfig) o;
       return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public int getRetry(){
        return retryConfig.getTimes();
    }

    private static  class   RetryConfig{
        //尝试次数
        private  int times;

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }
    }

    /**
     * 添加规则过滤器
     * @param filterConfig
     * @return
     */
    public  boolean addFilterConfig(FilterConfig filterConfig){
        return filterConfigSet.add(filterConfig);
    }

    /**
     * 根据id获取规则
     * @param id
     * @return
     */
    public FilterConfig getFilterConfigById(String id){
        for (FilterConfig filterConfig:filterConfigSet){
            if (filterConfig.getId().equalsIgnoreCase(id))return filterConfig;
        }
    return null;
    }

    /**
     * 判断是否存在规则
     * @param id
     * @return
     */
    public  boolean hashId(String id){
        for (FilterConfig filterConfig:filterConfigSet){
            if (filterConfig.getId().equalsIgnoreCase(id))return true;
        }
        return false;
    }

    public  static  class FlowCtlConfig{
        /**
         * 限流类型, Path 与 IP 与 服务对象
         */
        private  String type;

        /**
         * 限流对象
         */
        private String value;

        /**
         *  限流模式 单机与分布式;
         */
        private  String model;

        /**
         *  规则  json类型的数据
         */
        private  String config;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }
    }
    @Data
    public  static  class  HystrixConfig{
        private  String  path;
        private  int timeoutInMillinseconds;
        private  String fallbackResponse;
        private  int threadCoreSize;
    }
}
