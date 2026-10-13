package com.xq.web.goods_order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.mapper.GoodsOrderMapper;
import com.xq.web.home.entity.EchartItem;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {
    @Override
    public List<EchartItem> hotGoods() {
        return this.baseMapper.hotGoods();
    }

    @Override
    public List<EchartItem> hotCards() {
        return this.baseMapper.hotCards();
    }

    @Override
    public List<EchartItem> hotCourse() {
        return  this.baseMapper.hotCourse();
    }
}
