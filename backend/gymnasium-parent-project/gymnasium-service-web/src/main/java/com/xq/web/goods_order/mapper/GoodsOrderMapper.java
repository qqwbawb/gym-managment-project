package com.xq.web.goods_order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.home.entity.EchartItem;

import java.util.List;

public interface GoodsOrderMapper extends BaseMapper<GoodsOrder> {

    //查询热销商品
    List<EchartItem> hotGoods();
    //查询热销卡
    List<EchartItem> hotCards();
    //查询热销课程
    List<EchartItem> hotCourse();
}
