package com.xq.web.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    @TableField(exist = false)
    private Long roleId;

    private String name;
    private String sex;
    private String phone;
    private Integer age;
    private String birthday;
    private Integer height;
    private Integer weight;
    private Integer waist;
    private String joinTime;
    private String endTime;
    private String username;
    private String password;
    private String status;

    private String cardType;
    private Integer cardDay;
    private BigDecimal money;
    private BigDecimal price;
}
