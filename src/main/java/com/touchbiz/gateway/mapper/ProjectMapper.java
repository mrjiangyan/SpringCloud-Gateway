package com.touchbiz.gateway.mapper;

import com.touchbiz.db.starter.mapper.TkMapper;
import com.touchbiz.gateway.domain.ProjectDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProjectMapper extends TkMapper<ProjectDO> {

    /**
     * @param projectCode
     * @return
     */
    @Select("SELECT project_id, project_name,project_code,api_secret FROM project WHERE project_code = #{projectCode}")
    @Results(id = "Project", value = {@Result(column = "project_id", property = "projectId", javaType = Long.class),
            @Result(column = "project_name", property = "projectName", javaType = String.class),
            @Result(column = "project_code", property = "projectCode", javaType = String.class),
            @Result(column = "api_secret", property = "apiSecret", javaType = String.class)})
    ProjectDO getByAppKey(@Param("projectCode") String projectCode);

}
