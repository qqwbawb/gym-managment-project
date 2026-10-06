package com.xq.web.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.equipment.entity.ListParam;
import com.xq.web.equipment.entity.Material;
import com.xq.web.equipment.service.MaterialService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @PostMapping
    public ResultVo addMaterial(@RequestBody Material material){
        if(materialService.save(material)){
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error("新增失败");
    }

    @PutMapping
    public ResultVo updateMaterial(@RequestBody Material material){
        if(materialService.updateById(material)){
            return ResultUtils.success("编辑成功");
        }
        return ResultUtils.error("编辑失败");
    }

    @DeleteMapping("/{id}")
    public ResultVo deleteMaterial(@PathVariable("id") Long id){
        if(materialService.removeById(id)){
            return ResultUtils.success("删除成功");
        }
        return ResultUtils.error("删除失败");
    }

    @GetMapping("/list")
    public ResultVo listMaterial(ListParam listParam){
        IPage<Material> page=new Page<>(listParam.getCurrentPage(),listParam.getPageSize());

        QueryWrapper<Material> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotEmpty(listParam.getName())) {
            queryWrapper.lambda().like(Material::getName, listParam.getName());
        }
        IPage<Material> materialList=materialService.page(page,queryWrapper);
        return ResultUtils.success("查询成功",materialList);
    }

}
