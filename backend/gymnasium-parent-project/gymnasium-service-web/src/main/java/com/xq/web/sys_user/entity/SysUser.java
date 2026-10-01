package com.xq.web.sys_user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    @TableField(exist = false)//
    private Long roleId;

    private String username;
    private String password;
    private String email;
    private String phone;
    private String sex;
    private BigDecimal salary;
    private String userType;
    private String status;
    private String idAdmin;
    private boolean isAccountNonExpired=true;
    private boolean isAccountNonLocked=true;
    private boolean isCredentialsNonExpired=true;
    private boolean isEnabled=true;
    private String nickName;
    private Date createTime;
    private Date updateTime;


}
