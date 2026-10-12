package com.xq.web.goods_order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.mapper.GoodsOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {

}
