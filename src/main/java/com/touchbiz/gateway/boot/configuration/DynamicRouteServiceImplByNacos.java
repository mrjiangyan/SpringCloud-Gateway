package com.touchbiz.gateway.boot.configuration;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.touchbiz.common.utils.tools.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author Steven jiang
 * @date 2020/06/31
 */
@Component
public class DynamicRouteServiceImplByNacos implements CommandLineRunner {

    @Autowired
    private DynamicRouteServiceImpl dynamicRouteService;

    @Autowired
    private NacosConfigProperties configService;

    private static final String DATA_ID = "GATEWAY_CONFIG";

    @Value("${spring.application.name}")
    private String groupId;


    /**
     * 监听Nacos Server下发的动态路由配置
     */
    public void dynamicRouteByNacosListener() {
        try {
            String config = configService.configServiceInstance().getConfig(DATA_ID, groupId, 3000);
            updateData(config);
            configService.configServiceInstance().addListener(DATA_ID, groupId, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    updateData(configInfo);
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    private void updateData(String configInfo) {
        if (StringUtils.isEmpty(configInfo)) {
            return;
        }
        try {
            List<RouteDefinition> list = JsonUtils.json2list(configInfo, RouteDefinition.class);
            list.forEach(definition -> dynamicRouteService.update(definition));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) {
        dynamicRouteByNacosListener();
    }

}