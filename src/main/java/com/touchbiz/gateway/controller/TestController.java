package com.touchbiz.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RefreshScope
@RestController
public class TestController {

    @Value("${globelIgnoreUrls:}")
    private String globelIgnoreUrls;

    @GetMapping("/test/api-docs")
    public Mono<String> get(){
        return Mono.just(globelIgnoreUrls);
    }
}
