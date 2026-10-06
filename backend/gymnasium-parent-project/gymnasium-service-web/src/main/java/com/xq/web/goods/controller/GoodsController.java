package com.xq.web.goods.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.goods.entity.Goods;
import com.xq.web.goods.entity.GoodsParam;
import com.xq.web.goods.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public ResultVo addGoods(@RequestBody Goods goods){
        if(goodsService.save(goods)){
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error("新增失败");
    }

    @PutMapping
    public ResultVo updateGoods(@RequestBody Goods goods){
        if(goodsService.updateById(goods)){
            return ResultUtils.success("编辑成功");
        }
        return ResultUtils.error("编辑失败");
    }

    @DeleteMapping("/{goodsId}")
    public ResultVo deleteGoods(@PathVariable("goodsId") Long goodsId){
        if(goodsService.removeById(goodsId)){
            return ResultUtils.success("删除成功");
        }
        return ResultUtils.error("删除失败");
    }

    @GetMapping("/list")
    public ResultVo listGoods(GoodsParam goodsParam){
        IPage<Goods> page=new Page<>(goodsParam.getCurrentPage(),goodsParam.getPageSize());

        QueryWrapper<Goods> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotBlank(goodsParam.getName())){
            queryWrapper.lambda().like(Goods::getName,goodsParam.getName());
        }
        IPage<Goods> goodsPage=goodsService.page(page,queryWrapper);
        return ResultUtils.success("查询成功",goodsPage);
    }
}
