package com.touchbiz.gateway.domain;

import com.touchbiz.db.starter.domain.BaseDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectDO extends BaseDomain {

    /**
     * 项目私钥
     */
    private String apiSecret;

    private Long projectId;

    private String projectName;

    private String projectCode;
}
