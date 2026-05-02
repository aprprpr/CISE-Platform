package com.itheima.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("project_member_contribution")
public class ProjectMemberContribution {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    @TableField("user_id")
    private Integer userId;
    @TableField("contribution_rate")
    private Double contributionRate;
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
