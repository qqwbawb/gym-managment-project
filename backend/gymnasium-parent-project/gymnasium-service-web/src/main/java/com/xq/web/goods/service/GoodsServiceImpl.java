package com.xq.web.goods.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.goods.entity.Goods;
import com.xq.web.goods.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    /**
     * 库存原子扣减。
     * 一条 SQL 完成"判断 + 扣减"，靠数据库行锁保证原子性，不存在先查后改的并发窗口。
     */
    @Override
    public boolean subStore(Long goodsId, Integer num) {
        if (goodsId == null || num == null || num <= 0) {
            return false;
        }
        // 影响行数为 1 才代表扣减成功；为 0 说明库存不足或商品不存在
        return this.baseMapper.subStore(goodsId, num) > 0;
    }

    @Override
    public void addStore(Long goodsId, Integer num) {
        if (goodsId == null || num == null || num <= 0) {
            return;
        }
        this.baseMapper.addStore(goodsId, num);
    }
}
