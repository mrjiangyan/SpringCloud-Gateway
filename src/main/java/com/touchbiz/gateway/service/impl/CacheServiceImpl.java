package com.touchbiz.gateway.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.touchbiz.gateway.domain.ProjectDO;
import com.touchbiz.gateway.mapper.ProjectMapper;
import com.touchbiz.gateway.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheServiceImpl implements ICacheService {

    @Autowired
    private ProjectMapper projectMapper;

    private final LoadingCache<String, ProjectDO> projectCacheByKeySecret = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS).maximumSize(128)
            .build(
                    new CacheLoader<String, ProjectDO>() {
                        @Override
                        public ProjectDO load(String appKey) {
                            return projectMapper.getByAppKey(appKey);
                        }
                    }
            );

    @Override
    public ProjectDO getProject(String appKey) {
        try {
            return projectCacheByKeySecret.get(appKey);
        } catch (Exception e) {
            log.error("缓存获取失败", e);
            return null;
        }
    }
}
