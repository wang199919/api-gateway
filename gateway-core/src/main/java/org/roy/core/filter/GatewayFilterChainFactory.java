package org.roy.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.roy.common.rules.Rule;
import org.roy.core.context.GatewayContext;

import java.util.*;

/**
 * @author: roy
 * @date: 2023/10/30 14:11
 * @description:
 */
@Slf4j
public class GatewayFilterChainFactory implements  FilterFactory{
    private  static  class SingletonInstance{
        private  static final GatewayFilterChainFactory instance=new GatewayFilterChainFactory();

    }


    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.instance;
    }
    private GatewayFilterChainFactory() {
        ServiceLoader<Filter> serviceLoader=ServiceLoader.load(Filter.class);
        serviceLoader.stream().forEach(filterProvider -> {
            Filter filter=filterProvider.get();
            FilterAspect annotation=filter.getClass().getAnnotation(FilterAspect.class);
            log.info("load filter success: {},{},{},{}",filter.getClass(),annotation.id(),annotation.name(),annotation.order());
            if (annotation!=null){
                String filterId=annotation.id();
                if(StringUtils.isEmpty(filterId)){
                    filterId=filter.getClass().getName();
                }
                proceessorFilterIdMap.put(filterId,filter);
            }
        });
    }

    public  Map<String,Filter> proceessorFilterIdMap=new HashMap<>();
    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext context) throws Exception {
        GatewayFilterChain chain=new GatewayFilterChain();
        List<Filter> filters=new ArrayList<>();
        Rule rule=context.getRule();
        if (rule!=null){
            Set<Rule.FilterConfig> filterConfigSet=rule.getFilterConfigSet();
            Iterator iterator=filterConfigSet.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()){
                filterConfig= (Rule.FilterConfig) iterator.next();
                if(filterConfig==null)continue;
                String filterId=filterConfig.getId();
                if (StringUtils.isNoneEmpty(filterId)&&getFilterInfo(filterId)!=null){
                    Filter filter=getFilterInfo(filterId);
                    filters.add(filter);
                }
            }
        }
        //todo 添加路由过滤器
        //排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));
        //添加到链表
        chain.addAllFilter(filters);
        return null;
    }

    @Override
    public Filter getFilterInfo(String filterId) throws Exception {
        return proceessorFilterIdMap.get(filterId);
    }
}
