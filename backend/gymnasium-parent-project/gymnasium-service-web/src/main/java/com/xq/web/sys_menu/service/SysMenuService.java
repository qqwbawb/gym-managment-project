package com.xq.web.sys_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.sys_menu.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> getParent();

    //根据员工id查询菜单信息
    List<SysMenu> getMenuByUserId(Long userId);
    //根据会员id查询菜单信息
    List<SysMenu> getMenuByMemberId(Long memberId);
    //根据角色id查询菜单信息
    List<SysMenu> getMenuByRoleId(Long roleId);
}
