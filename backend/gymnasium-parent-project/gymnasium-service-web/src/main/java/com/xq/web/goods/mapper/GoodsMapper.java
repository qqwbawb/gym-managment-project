package com.xq.web.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xq.web.goods.entity.Goods;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 库存原子扣减（方案一：SQL 原子操作）
     * 判断与扣减在同一条 SQL 内完成，由数据库行锁保证原子性，避免超卖。
     *
     * @param goodsId 商品 id
     * @param num     扣减数量
     * @return 影响行数：1 = 扣减成功；0 = 库存不足或商品不存在
     */
    int subStore(@Param("goodsId") Long goodsId, @Param("num") Integer num);

    /**
     * 库存回补（订单取消 / 退货场景）
     *
     * @param goodsId 商品 id
     * @param num     回补数量
     * @return 影响行数
     */
    int addStore(@Param("goodsId") Long goodsId, @Param("num") Integer num);

    /**
     * 悲观锁查询商品（方案二：SELECT ... FOR UPDATE）
     * 锁定该商品行，后续在同一事务内可放心做多条件联合校验。
     * 注意：必须在事务中调用，否则锁会在语句结束后立即释放。
     *
     * @param goodsId 商品 id
     * @return 商品信息
     */
    Goods getGoodsForUpdate(@Param("goodsId") Long goodsId);
}
