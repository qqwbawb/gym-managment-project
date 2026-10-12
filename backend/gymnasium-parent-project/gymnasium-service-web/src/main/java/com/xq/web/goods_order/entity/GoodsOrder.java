package com.xq.web.goods_order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("goods_order")
public class GoodsOrder {

    @TableId(type = IdType.AUTO)
    private Long orderId;

    private Long goodsId;
    private String name;
    private String details;
    private String image;
    private BigDecimal price;
    private  String unit;
    private  String specs;
    private  Integer num;
    private BigDecimal totalPrice;
    private String controlUser;

}
