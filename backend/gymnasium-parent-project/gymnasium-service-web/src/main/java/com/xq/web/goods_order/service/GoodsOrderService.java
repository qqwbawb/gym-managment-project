package com.xq.web.goods_order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.home.entity.EchartItem;

import java.util.List;

public interface GoodsOrderService extends IService<GoodsOrder> {
    //查询热销商品
    List<EchartItem> hotGoods();
    //查询热销卡
    List<EchartItem> hotCards();
    //查询热销课程
    List<EchartItem> hotCourse();
}
