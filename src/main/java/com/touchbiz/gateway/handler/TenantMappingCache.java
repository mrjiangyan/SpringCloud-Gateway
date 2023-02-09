//package org.aiprime.gateway.handler;
//
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.common.util.RedisUtil;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * 路由刷新监听（实现方式：redis监听handler）
// * @author zyf
// * @date: 2022/4/21 10:55
// */
//@Slf4j
//@Component
//public class TenantMappingCache {
//
//    private final String PREFIX = "TENANT_HOST_";
//    LoadingCache<String, String> graphs = CacheBuilder.newBuilder()
//            .maximumSize(16).expireAfterWrite(60, TimeUnit.MINUTES)
//            .build(
//                    new CacheLoader<>() {
//                        public String load(String host) { // no checked exception
//                            Object tenantId = redisUtil.get(PREFIX + host);
//                            return tenantId != null ? String.valueOf(tenantId) : "";
//                        }
//                    });
//
//    private final RedisUtil redisUtil;
//
//    public TenantMappingCache(RedisUtil redisUtil) {
//        this.redisUtil = redisUtil;
//    }
//
//    @SneakyThrows
//    public String getTenantId(String host) {
//        return graphs.get(host);
//    }
//
//}