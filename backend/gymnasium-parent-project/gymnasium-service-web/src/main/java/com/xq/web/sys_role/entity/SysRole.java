package com.xq.web.sys_role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long roleId;
    private String roleName;
    private String remark;
    private Date createTime;
    private Date updateTime;

    /**
     * 角色类型 1：员工类型 2：会员类型
     * 注意：数据库列名为 types（非 type），不能省略 s
     */
    private String types;


}
