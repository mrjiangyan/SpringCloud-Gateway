//package org.aiprime.gateway.filter;
//
//import lombok.extern.slf4j.Slf4j;
//import org.aiprime.gateway.handler.TenantMappingCache;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.util.ObjectUtils;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//
//import static org.springframework.http.HttpHeaders.REFERER;
//
///**
//* 全局拦截器，作用所有的微服务
//*
//* 1.重写StripPrefix(获取真实的URL)
//* 2.将现在的request，添加当前身份
//* @author: scott
//* @date: 2022/4/8 10:55
//*/
//@Slf4j
//@Component
//public class TenantFilter implements GlobalFilter, Ordered {
//
//    @Autowired
//    private TenantMappingCache tenantMappingCache;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        log.info("TenantFilter");
//        //根据refer去查询对应的租户，如果没有则根据host去匹配
//        String referer = exchange.getRequest().getHeaders().getFirst(REFERER);
//        String tenantId = null;
//        if(!ObjectUtils.isEmpty(referer)) {
//            URI uri = URI.create(referer);
//            tenantId = tenantMappingCache.getTenantId(uri.getHost());
//        }
//
//        if(ObjectUtils.isEmpty(tenantId)){
//            tenantId = tenantMappingCache.getTenantId(exchange.getRequest().getURI().getHost());
//        }
//        //如果找不到就用1来作为默认的租户id
//        if(ObjectUtils.isEmpty(tenantId)){
//            tenantId = "1";
//        }
//        //2.将现在的request，添加当前身份
//        ServerHttpRequest mutableReq = exchange.getRequest().mutate().header("tenantId", tenantId).build();
//        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
//        return chain.filter(mutableExchange);
//    }
//
//    @Override
//    public int getOrder() {
//        return 1;
//    }
//
//}
