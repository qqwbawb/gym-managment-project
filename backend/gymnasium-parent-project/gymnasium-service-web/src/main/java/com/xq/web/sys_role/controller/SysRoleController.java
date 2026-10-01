package com.xq.web.sys_role.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.sys_role.entity.RoleParm;
import com.xq.web.sys_role.entity.SelectType;
import com.xq.web.sys_role.entity.SysRole;
import com.xq.web.sys_role.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    //新增角色信息
    @PostMapping
    public ResultVo addRole(@RequestBody SysRole Role){
        Role.setCreateTime(new Date());
        boolean save= sysRoleService.save(Role);
        if(save){
            return ResultUtils.success("新增成功");
        }else{
            return ResultUtils.error("新增失败");
        }
    }

    //编辑角色
    @PutMapping
    public ResultVo editRole(@RequestBody SysRole Role){
        Role.setUpdateTime(new Date());
        boolean save= sysRoleService.updateById(Role);
        if(save){
            return ResultUtils.success("编辑成功");
        }else{
            return ResultUtils.error("编辑失败");
        }
    }

    //删除角色
    @DeleteMapping("/{roleId}")
    public ResultVo deleteRole(@PathVariable("roleId") Long roleId){
        boolean b=sysRoleService.removeById(roleId);
        if(b){
            return ResultUtils.success("删除成功");
        }else{
            return ResultUtils.error("删除失败");
        }
    }

    //列表分页查询
    @GetMapping("/list")
    public ResultVo getRole(RoleParm roleParm){
        IPage<SysRole> list = sysRoleService.list(roleParm);
        return ResultUtils.success("查询成功",list);
    }

    //查询页面需要显示的角色信息
    @GetMapping("/getSelect")
    public ResultVo getSelect(){
        List<SysRole> list = sysRoleService.list();
        List<SelectType>  selectTypeList=new ArrayList<>();
        if(list!=null&&list.size()>0){
            list.stream().forEach(item->{
                SelectType type=new SelectType();
                type.setValue(item.getRoleId());
                type.setLabel(item.getRoleName());
                selectTypeList.add(type);
            });
        }
        return ResultUtils.success("查询成功",selectTypeList);
    }
}
