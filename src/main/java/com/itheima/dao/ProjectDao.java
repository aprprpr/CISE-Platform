package com.itheima.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectDao extends BaseMapper<Project> {

    @Select("SELECT user_id as userId FROM project_member_contribution WHERE project_id = #{projectId}")
    List<Map<String, Object>> getProjectMembers(Integer projectId);
}