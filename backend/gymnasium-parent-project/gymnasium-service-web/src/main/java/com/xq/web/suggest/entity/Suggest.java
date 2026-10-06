package com.xq.web.suggest.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
@Data
@TableName("suggest")
public class Suggest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Date dateTime;
}
