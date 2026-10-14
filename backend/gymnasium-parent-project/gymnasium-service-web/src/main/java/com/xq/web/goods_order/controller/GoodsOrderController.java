package com.xq.web.goods_order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.goods.entity.GoodsParam;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.entity.OrderParam;
import com.xq.web.goods_order.service.GoodsOrderService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class GoodsOrderController {

    @Autowired
    private GoodsOrderService goodsOrderService;

    /**
     * 下单（默认走 SQL 原子扣减库存方案）
     * 业务逻辑与事务边界都在 Service 层，Controller 只负责接收参数、包装返回结果。
     */
    @PostMapping("/down")
    @PreAuthorize("hasAuthority('sys:orderList:down')")
    public ResultVo down(@RequestBody OrderParam orderParam) {
        try {
            goodsOrderService.down(orderParam);
            return ResultUtils.success("下单成功");
        } catch (RuntimeException e) {
            //库存不足等业务失败：事务已回滚，返回友好提示
            return ResultUtils.error(e.getMessage());
        }
    }

    /**
     * 下单（悲观锁方案，用于对比/演示两种并发控制实现）
     * 访问路径：POST /api/order/downWithLock
     */
    @PostMapping("/downWithLock")
    @PreAuthorize("hasAuthority('sys:orderList:down')")
    public ResultVo downWithLock(@RequestBody OrderParam orderParam) {
        try {
            goodsOrderService.downWithLock(orderParam);
            return ResultUtils.success("下单成功（悲观锁方案）");
        } catch (RuntimeException e) {
            return ResultUtils.error(e.getMessage());
        }
    }

    //查询订单列表
    @GetMapping("/list")
    public ResultVo list(GoodsParam goodsParam) {
        IPage<GoodsOrder> page = new Page<>(goodsParam.getCurrentPage(), goodsParam.getPageSize());
        QueryWrapper<GoodsOrder> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(goodsParam.getName())) {
            queryWrapper.lambda().like(GoodsOrder::getName, goodsParam.getName());
        }
        IPage<GoodsOrder> goodsOrderIPage = goodsOrderService.page(page, queryWrapper);
        return ResultUtils.success("查询成功", goodsOrderIPage);
    }

}
