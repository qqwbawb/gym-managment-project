package com.xq.web.sys_role.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.sys_menu.entity.MakeMenuTree;
import com.xq.web.sys_menu.entity.SysMenu;
import com.xq.web.sys_menu.service.SysMenuService;
import com.xq.web.sys_role.entity.RoleAssignParam;
import com.xq.web.sys_role.entity.RolePermissionVo;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import com.xq.web.sys_role.entity.RoleParm;
import com.xq.web.sys_role.entity.SysRole;
import com.xq.web.sys_role.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public IPage<SysRole> list(RoleParm roleParm) {
        //构造分页对象
        IPage<SysRole> page=new Page<>();
        //配置分页对象
        page.setSize(roleParm.getPageSize());
        page.setCurrent(roleParm.getCurrentPage());
        //构造分页查询条件
        QueryWrapper<SysRole>  query=new QueryWrapper<>();
        if(StringUtils.isNotBlank(roleParm.getRoleName())) {//判断不为空
            query.lambda().like(SysRole::getRoleName,roleParm.getRoleName());
        }
        return this.baseMapper.selectPage(page,query);
    }

    @Autowired
    private SysUserService sysUserService;

    @Override
    public RolePermissionVo getMenuTree(RoleAssignParam roleAssignParam) {
        //查询用户信息
        SysUser user=sysUserService.getById(roleAssignParam.getUserId());
        List<SysMenu> list=null;
        if(StringUtils.isNotEmpty(user.getIsAdmin())&&user.getIsAdmin().equals("1")) {
            //超级管理员，直接查询所以的菜单信息
            list=sysMenuService.list();
        }else{
            list=sysMenuService.getMenuByUserId(roleAssignParam.getUserId());
        }
        //组装树形数据
        List<SysMenu> menuList= MakeMenuTree.makeTree(list,0L);
        //查询角色原来的菜单分配信息
        List<SysMenu> roleList = sysMenuService.getMenuByRoleId(roleAssignParam.getRoleId());
        List<Long> ids=new ArrayList<>();
        Optional.ofNullable(roleList).orElse(new ArrayList<>())
                .stream()
                .filter(item->item!=null)
                .forEach(item->{
                    ids.add(item.getMenuId());
                });
        //组装数据
        RolePermissionVo rolePermissionVo=new RolePermissionVo();
        rolePermissionVo.setListmenu(menuList);
        rolePermissionVo.setCheckList(ids.toArray());
        return rolePermissionVo;
    }
}
