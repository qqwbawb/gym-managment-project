package com.xq.web.sys_menu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.sys_menu.entity.MakeMenuTree;
import com.xq.web.sys_menu.entity.SysMenu;
import com.xq.web.sys_menu.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper,SysMenu> implements SysMenuService {
    @Override
    public List<SysMenu> getMenuByUserId(Long userId) {
        return this.baseMapper.getMenuByUserId(userId);
    }

    @Override
    public List<SysMenu> getMenuByMemberId(Long memberId) {
        return this.baseMapper.getMenuByMemberId(memberId);
    }

    @Override
    public List<SysMenu> getMenuByRoleId(Long roleId) {
        return this.baseMapper.getMenuByRoleId(roleId);
    }

    @Override
    public List<SysMenu> getParent() {
        //查询目录和菜单
        String[] type={"0","1"};
        List<String> strings = Arrays.asList(type);
        //构造查询条件
        QueryWrapper<SysMenu> query = new QueryWrapper();
        query.lambda().in(SysMenu::getType,strings).orderByAsc(SysMenu::getOrderNum);
        List<SysMenu> menus = this.baseMapper.selectList(query);
        //组装顶级菜单
        SysMenu menu = new  SysMenu();
        menu.setMenuId(0L);
        menu.setParentId(-1L);
        menus.add(menu);
        return MakeMenuTree.makeTree(menus,-1L);
    }
}
