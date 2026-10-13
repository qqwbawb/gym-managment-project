package com.xq.web.goods_order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.goods.entity.Goods;
import com.xq.web.goods.entity.GoodsParam;
import com.xq.web.goods.service.GoodsService;
import com.xq.web.goods_order.entity.GoodsOrder;
import com.xq.web.goods_order.entity.OrderItem;
import com.xq.web.goods_order.entity.OrderParam;
import com.xq.web.goods_order.service.GoodsOrderService;
import com.xq.web.home.entity.Echart;
import com.xq.web.home.entity.EchartItem;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class GoodsOrderController {

    @Autowired
    private GoodsOrderService goodsOrderService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private GoodsService goodsService;

    //下单
    @PostMapping("/down")
    public ResultVo down(@RequestBody OrderParam orderParam){
        //查询用户信息
        SysUser user = sysUserService.getById(orderParam.getUserId());
        List<OrderItem> list = orderParam.getOrderList();
        List<GoodsOrder> orderList=new ArrayList<>();
        for(OrderItem orderItem:list){
            Long goodsId = orderItem.getGoodsId();
            Integer num = orderItem.getNum();

            Goods good=goodsService.getById(goodsId);
            GoodsOrder goodsOrder=new GoodsOrder();
            BeanUtils.copyProperties(good,goodsOrder);
            goodsOrder.setNum(num);
            BigDecimal number=new BigDecimal(num);
            goodsOrder.setTotalPrice(good.getPrice().multiply(number));
            goodsOrder.setControlUser(user.getNickName());
            orderList.add(goodsOrder);
        }
        if(orderList.size()>0){
            goodsOrderService.saveBatch(orderList);
        }
        return ResultUtils.success("下单成功");
    }

    //查询订单列表
    @GetMapping("/list")
    public ResultVo list(GoodsParam goodsParam){
        IPage<GoodsOrder> page=new Page<>(goodsParam.getCurrentPage(),goodsParam.getPageSize());
        QueryWrapper<GoodsOrder> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotEmpty(goodsParam.getName())){
            queryWrapper.lambda().like(GoodsOrder::getName,goodsParam.getName());
        }
        IPage<GoodsOrder> goodsOrderIPage=goodsOrderService.page(page,queryWrapper);
        return ResultUtils.success("查询成功",goodsOrderIPage);
    }

}
