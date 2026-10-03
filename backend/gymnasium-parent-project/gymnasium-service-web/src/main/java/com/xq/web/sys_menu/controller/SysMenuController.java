package com.xq.web.sys_menu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.sys_menu.entity.MakeMenuTree;
import com.xq.web.sys_menu.entity.SysMenu;
import com.xq.web.sys_menu.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    //新增
    @PostMapping
    public ResultVo addMenu(@RequestBody SysMenu sysMenu){
        sysMenu.setCreateTime(new Date());
        if(sysMenuService.save(sysMenu)){
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error("新增失败");
    }

    //编辑
    @PutMapping
    public ResultVo editMenu(@RequestBody SysMenu sysMenu){
        sysMenu.setUpdateTime(new Date());
        if(sysMenuService.updateById(sysMenu)){
            return ResultUtils.success("编辑成功");
        }
        return ResultUtils.error("编辑失败");
    }
    //删除
    @DeleteMapping("/{menuId}")
    public ResultVo deleteMenu(@PathVariable("menuId") Long menuId){
        if(sysMenuService.removeById(menuId)){
            return ResultUtils.success("删除成功");
        }
        return ResultUtils.error("删除失败");
    }
    //查询
    @GetMapping("/list")
    public ResultVo listMenu(){
        QueryWrapper<SysMenu> query = new QueryWrapper<>();
        query.lambda().orderByAsc(SysMenu::getOrderNum);
        List<SysMenu> list = sysMenuService.list(query);
        //组装树形菜单的数据
        List<SysMenu> menuList = MakeMenuTree.makeTree(list,0L);
        return ResultUtils.success("查询成功",menuList);
    }
    //查询上级菜单树
    @GetMapping("/parent")
    public ResultVo getParentMenu(){
        List<SysMenu> parent=sysMenuService.getParent();
        return ResultUtils.success("查询成功",parent);
    }
}
