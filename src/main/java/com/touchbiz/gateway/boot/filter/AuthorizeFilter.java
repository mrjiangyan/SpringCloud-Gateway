package com.touchbiz.gateway.boot.filter;

import com.touchbiz.common.entity.result.ApiResult;
import com.touchbiz.common.entity.result.IResultMsg;
import com.touchbiz.common.utils.tools.JsonUtils;
import com.touchbiz.gateway.domain.ProjectDO;
import com.touchbiz.gateway.service.ICacheService;
import com.touchbiz.gateway.utils.SignatureUtils;
import com.touchbiz.web.starter.configuration.HttpHeaderConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RefreshScope
public class AuthorizeFilter implements Ordered, GlobalFilter {

    private static final String APP_KEY = "appKey";

    private static final String APP_SECRET = "appSecret";

    private static final String CACHE_CONTROL = "no-store, no-cache, must-revalidate, max-age=0";

    @Value("${globelIgnoreUrls:/callback,swagger,api-docs}")
    private String globelIgnoreUrls;

    @Autowired
    private ICacheService cacheService;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().toString();
        boolean urlIgnoreFlag = this.validateIgnoreUrls(path);
        if (urlIgnoreFlag) {
            log.info("路径{}在无需鉴权的名单内，因此无需经过鉴权验证", path);
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        String appKey = headers.getFirst(APP_KEY);
        String secret = null;
        //是否为外部调用模式
        Boolean isExternalCall = false;
        if (headers.containsKey(HttpHeaderConstants.HEADER_APP_KEY)) {
            appKey = headers.getFirst(HttpHeaderConstants.HEADER_APP_KEY);
            isExternalCall = true;
        } else {
            secret = headers.getFirst(APP_SECRET);
        }

        //假如header或者cookie中都找不到token
        if (!isExternalCall && (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(secret))) {
            log.warn("header中找不到相关配置");
            return throwAuthrizeFailed(response, IResultMsg.APIEnum.UNAUTHORIZED);
        }

        final ProjectDO project = cacheService.getProject(appKey);
        if (project == null) {
            log.warn("验证失败");
            return throwAuthrizeFailed(response, IResultMsg.APIEnum.UNAUTHORIZED);
        }

        HttpMethod method = request.getMethod();
        StringBuilder sb = new StringBuilder();
        boolean containBody = HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.DELETE.equals(method);
        if (containBody && isExternalCall) {
            //重新构造request，参考ModifyRequestBodyGatewayFilterFactory
            sb.append(getBodyFromRequest(exchange.getRequest()));
        }

        if (!isExternalCall) {
            if (!project.getApiSecret().equals(secret)) {
                log.warn("内部访问缺少secret");
                return throwAuthrizeFailed(response, IResultMsg.APIEnum.UNAUTHORIZED);
            }
        } else {
            final String sign = headers.getFirst(HttpHeaderConstants.HEADER_SIGN);
            final String timestamp = headers.getFirst(HttpHeaderConstants.HEADER_TIMESTAMP);
            final String newSign = SignatureUtils.sign(path, project.getProjectCode(), project.getApiSecret(), timestamp,
                    sb.toString());
            if (!newSign.equals(sign)) {
                log.warn("签名验证失败,{},{}", sign, newSign);
                return throwAuthrizeFailed(response, IResultMsg.APIEnum.UNAUTHORIZED);
            }
            // 银商资讯只能调用部分接口 需要做验证
            List<String> appKeys = new ArrayList<>();
            appKeys.add("101123");
            appKeys.add("101127");
            appKeys.add("128185");
            if(appKeys.contains(appKey)){
                log.info("银商资讯只能调用部分接口");
                if(!path.contains("yszx")){
                    log.warn("权限url验证失败,{}",path);
                    return throwAuthrizeFailed(response, IResultMsg.APIEnum.UNAUTHORIZED);
                }
            }
        }

        if (containBody && isExternalCall) {
            return chain.filter(exchange.mutate().build());
        } else {
            return chain.filter(exchange);
        }
    }



    @Override
    public int getOrder()
    {
        return -100;
    }
    /**
     * 判断这个路劲是否是需要被忽略的
     *
     * @param path
     * @return
     */
    private boolean validateIgnoreUrls(String path) {
        //假如没有配置任何需要忽略的路径
        if (StringUtils.isEmpty(globelIgnoreUrls)) {
            return false;
        }
        String[] urlArray = globelIgnoreUrls.split(",");
        boolean flag = false;
        for (String url : urlArray) {
            if (path.contains(url)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private Mono<Void> throwAuthrizeFailed(ServerHttpResponse response, IResultMsg resultMsg) {
        //不合法(响应未登录的异常)
        //设置响应的headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
        //设置body
        String warningStr = JsonUtils.toJson(ApiResult.getCustomResponse(resultMsg));
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(warningStr.getBytes());
        return response.writeWith(Mono.just(bodyDataBuffer));
    }

    private String getBodyFromRequest(ServerHttpRequest serverHttpRequest){
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        StringBuilder sb = new StringBuilder();
        body.subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            String bodyString = new String(bytes, StandardCharsets.UTF_8);
            sb.append(bodyString);
        });
        log.info("请求体内容;{}",sb.toString());
        return sb.toString();
    }


}