package com.touchbiz.gateway.config;

import com.touchbiz.gateway.handler.JsonExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;

/**
 * @description 网关异常全局配置
 * @date 2020-12-23
 */
@Configuration
public class GlobalExceptionConfig {

    /**
     * 注入自定义网关异常处理器，用于标准化响应
     * @param viewResolversProvider
     * @param serverCodecConfigurer
     * @return
     */
    @Primary
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                                             ServerCodecConfigurer serverCodecConfigurer) {
        return new JsonExceptionHandler(
                viewResolversProvider.getIfAvailable(Collections::emptyList),
                serverCodecConfigurer.getWriters(),
                serverCodecConfigurer.getReaders()
        );
    }
}
