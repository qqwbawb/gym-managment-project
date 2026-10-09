package com.xq.web.sys_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xq.web.sys_menu.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    //根据员工id查询菜单数据
    List<SysMenu> getMenuByUserId(@Param("userId") Long userId);
    //根据会员id查询菜单数据
    List<SysMenu> getMenuByMemberId(@Param("memberId") Long memberId);
    //根据角色id查询菜单数据
    List<SysMenu> getMenuByRoleId(@Param("roleId") Long roleId);

}
