package com.xq.web.goods.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.goods.entity.Goods;

public interface GoodsService extends IService<Goods> {

    /**
     * 库存原子扣减（购物车下单场景，方案一）
     *
     * @param goodsId 商品 id
     * @param num     扣减数量
     * @return true = 扣减成功；false = 库存不足
     */
    boolean subStore(Long goodsId, Integer num);

    /**
     * 库存回补
     *
     * @param goodsId 商品 id
     * @param num     回补数量
     */
    void addStore(Long goodsId, Integer num);
}
