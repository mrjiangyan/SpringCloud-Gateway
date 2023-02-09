package com.touchbiz.gateway.filter;

import com.touchbiz.cache.starter.IRedisTemplate;
import com.touchbiz.common.entity.model.SysUserCacheInfo;
import com.touchbiz.common.utils.security.JwtUtil;
import com.touchbiz.common.utils.security.SensitiveInfoUtil;
import com.touchbiz.common.utils.tools.JsonUtils;
import com.touchbiz.webflux.starter.configuration.HttpHeaderConstants;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.util.RedisUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
* 全局拦截器，作用所有的微服务
*
* 1.重写StripPrefix(获取真实的URL)
* 2.将现在的request，添加当前身份
* @author: scott
* @date: 2022/4/8 10:55
*/
@Slf4j
@Component
public class GlobalAccessTokenFilter implements GlobalFilter, Ordered {
    public final static String X_ACCESS_TOKEN = "X-Access-Token";
    public final static String X_GATEWAY_BASE_PATH = "X_GATEWAY_BASE_PATH";

    private final RedisUtil redisUtil;

    private final IRedisTemplate redisTemplate;

    public GlobalAccessTokenFilter(RedisUtil redisUtil, IRedisTemplate redisTemplate) {
        this.redisUtil = redisUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        String scheme = request.getURI().getScheme();
        String host = request.getURI().getHost();
        int port = request.getURI().getPort();
        String basePath = scheme + "://" + host + ":" + port;
        // 1. 重写StripPrefix(获取真实的URL)
        addOriginalRequestUrl(exchange, request.getURI());
        String rawPath = request.getURI().getRawPath();
        String newPath = "/" + Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/")).skip(1L).collect(Collectors.joining("/"));
        log.info("rawPath:{},newPath:{}", rawPath, newPath);
        ServerHttpRequest newRequest = request.mutate().path(newPath).build();
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

        //判断是否存在token
        var token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userName = "";
        SysUserCacheInfo user = null;
        if(!ObjectUtils.isEmpty(token)){
            userName = JwtUtil.getUsername(token);
            log.info("userName:{}",userName);
            //如果能拿到用户名，则尝试获取到
            if(!ObjectUtils.isEmpty(userName)){
                user = getLoginUser(token);
            }

        }
        //2.将现在的request，添加当前身份
        ServerHttpRequest mutableReq = exchange.getRequest().mutate().header("Authorization-UserName", userName)
                .header(HttpHeaderConstants.HEADER_USER, user != null ? JsonUtils.toJson(user) : null)
                .header(X_GATEWAY_BASE_PATH,basePath).build();
        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
        return chain.filter(mutableExchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private SysUserCacheInfo getLoginUser(String token) {
        SysUserCacheInfo loginUser = null;
        String loginUserKey = CacheConstant.SYS_USERS_CACHE + token;
        //【重要】此处通过redis原生获取缓存用户，是为了解决微服务下system服务挂了，其他服务互调不通问题---
        if (redisUtil.hasKey(loginUserKey)) {
            try {
                String value = String.valueOf(redisTemplate.get(loginUserKey));
                log.info("redis:{}", value);
                loginUser = JsonUtils.toObject(value, SysUserCacheInfo.class);
                //解密用户
                SensitiveInfoUtil.handlerObject(loginUser, false);
                log.info("loginUser:{}", JsonUtils.toJson(loginUser));
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return loginUser;
    }

}
