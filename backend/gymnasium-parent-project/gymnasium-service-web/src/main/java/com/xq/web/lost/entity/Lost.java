package com.xq.web.lost.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("lost")
public class Lost {
    @TableId(type = IdType.AUTO)
    private Long lostId;
    private String lostName;
    private String foundTime;
    /**
     * 捡到地址。
     * 注意：数据库列名为 found_addres（原始 schema 拼写缺少末尾的 s），
     * 与 Java 字段的常规驼峰映射 found_address 不一致，因此显式指定列名。
     */
    @TableField("found_addres")
    private String foundAddress;
    private String foundPerson;
    private String foundPhone;
    private String status;
    private String lostPerson;
}