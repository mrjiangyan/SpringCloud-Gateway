package com.touchbiz.gateway.service;

import com.touchbiz.gateway.domain.ProjectDO;

/**
 * @author steven
 */
public interface ICacheService {

    /**
     * 查找项目信息
     *
     * @param appKey
     * @return
     */
    ProjectDO getProject(String appKey);

}
