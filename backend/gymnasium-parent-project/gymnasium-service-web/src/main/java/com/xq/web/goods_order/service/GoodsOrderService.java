package com.xq.web.goods_order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.entity.OrderParam;
import com.xq.web.home.entity.EchartItem;

import java.util.List;

public interface GoodsOrderService extends IService<GoodsOrder> {

    //查询热销商品
    List<EchartItem> hotGoods();
    //查询热销卡
    List<EchartItem> hotCards();
    //查询热销课程
    List<EchartItem> hotCourse();

    /**
     * 下单（原子扣减库存方案）
     * 在一个事务内完成：校验并扣减库存 → 生成订单。
     * 任一商品库存不足则整单失败并回滚，不会出现"扣了库存却没生成订单"的脏数据。
     *
     * @param orderParam 下单参数（操作人 + 购物车明细）
     * @throws RuntimeException 库存不足 / 商品不存在时抛出，触发事务回滚
     */
    void down(OrderParam orderParam);

    /**
     * 下单（悲观锁方案）
     * 用 SELECT ... FOR UPDATE 锁住商品行，在锁的保护下做多条件联合校验后扣减。
     * 与 {@link #down(OrderParam)} 二选一使用，用于对比两种并发控制方案。
     *
     * @param orderParam 下单参数
     * @throws RuntimeException 库存不足 / 商品不存在时抛出，触发事务回滚
     */
    void downWithLock(OrderParam orderParam);
}
