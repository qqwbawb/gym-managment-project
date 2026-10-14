package com.xq.web.goods_order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.goods.entity.Goods;
import com.xq.web.goods.mapper.GoodsMapper;
import com.xq.web.goods.service.GoodsService;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.entity.OrderItem;
import com.xq.web.goods_order.entity.OrderParam;
import com.xq.web.goods_order.mapper.GoodsOrderMapper;
import com.xq.web.home.entity.EchartItem;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SysUserService sysUserService;

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
        return this.baseMapper.hotCourse();
    }

    /**
     * 方案一：SQL 原子操作扣减库存。
     *
     * 事务边界说明：
     *   @Transactional 保证「扣库存」与「写订单」要么都成功、要么都回滚。
     *   库存扣减用一条 UPDATE ... WHERE store >= ? 完成判断与扣减，
     *   影响行数为 0 即库存不足，抛异常让整个事务回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void down(OrderParam orderParam) {
        //校验操作人
        SysUser user = sysUserService.getById(orderParam.getUserId());
        if (user == null) {
            throw new RuntimeException("操作人不存在");
        }
        List<OrderItem> list = orderParam.getOrderList();
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        List<GoodsOrder> orderList = new ArrayList<>();
        for (OrderItem orderItem : list) {
            Long goodsId = orderItem.getGoodsId();
            Integer num = orderItem.getNum();
            if (goodsId == null || num == null || num <= 0) {
                throw new RuntimeException("订单明细参数不合法");
            }

            //先查商品信息（用于计算金额、填充订单冗余字段）
            Goods good = goodsService.getById(goodsId);
            if (good == null) {
                throw new RuntimeException("商品不存在：" + goodsId);
            }

            //核心：原子扣减库存。判断与扣减在同一条 SQL 内完成，并发安全。
            boolean subSuccess = goodsService.subStore(goodsId, num);
            if (!subSuccess) {
                //库存不足 → 抛异常 → 整个事务回滚（前面已扣的库存也会一起回滚）
                throw new RuntimeException("商品【" + good.getName() + "】库存不足");
            }

            //组装订单
            GoodsOrder goodsOrder = new GoodsOrder();
            BeanUtils.copyProperties(good, goodsOrder);
            goodsOrder.setNum(num);
            goodsOrder.setTotalPrice(good.getPrice().multiply(new BigDecimal(num)));
            goodsOrder.setControlUser(user.getNickName());
            orderList.add(goodsOrder);
        }

        //批量写订单（与上面的扣库存同属一个事务）
        this.saveBatch(orderList);
    }

    /**
     * 方案二：悲观锁扣减库存。
     *
     * 与方案一的区别：
     *   down()        —— 一条 UPDATE 搞定，适合"单字段计数器"的原子加减，性能最好；
     *   downWithLock()—— SELECT ... FOR UPDATE 锁住商品行，锁在事务提交时才释放，
     *                    适合需要在锁保护下做【多条件联合校验】的场景
     *                    （例如：库存 + 限购数量 + 会员等级 一起判断）。
     *
     * 注意：FOR UPDATE 必须在事务内才有意义，否则语句执行完锁立即释放。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downWithLock(OrderParam orderParam) {
        SysUser user = sysUserService.getById(orderParam.getUserId());
        if (user == null) {
            throw new RuntimeException("操作人不存在");
        }
        List<OrderItem> list = orderParam.getOrderList();
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        List<GoodsOrder> orderList = new ArrayList<>();
        for (OrderItem orderItem : list) {
            Long goodsId = orderItem.getGoodsId();
            Integer num = orderItem.getNum();
            if (goodsId == null || num == null || num <= 0) {
                throw new RuntimeException("订单明细参数不合法");
            }

            //悲观锁：锁住这一行商品，其它事务的同类操作会在此阻塞等待
            Goods good = goodsMapper.getGoodsForUpdate(goodsId);
            if (good == null) {
                throw new RuntimeException("商品不存在：" + goodsId);
            }

            //锁保护下做校验，此处可以放心写复杂的联合判断逻辑
            if (good.getStore() == null || good.getStore() < num) {
                throw new RuntimeException("商品【" + good.getName() + "】库存不足");
            }

            //扣减库存（此时该行已被本事务锁定，直接更新即可）
            Goods update = new Goods();
            update.setGoodsId(goodsId);
            update.setStore(good.getStore() - num);
            goodsService.updateById(update);

            //组装订单
            GoodsOrder goodsOrder = new GoodsOrder();
            BeanUtils.copyProperties(good, goodsOrder);
            goodsOrder.setNum(num);
            goodsOrder.setTotalPrice(good.getPrice().multiply(new BigDecimal(num)));
            goodsOrder.setControlUser(user.getNickName());
            orderList.add(goodsOrder);
        }

        this.saveBatch(orderList);
    }
}
