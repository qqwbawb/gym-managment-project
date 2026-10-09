package com.xq.web.sys_role.entity;

import com.xq.web.sys_menu.entity.SysMenu;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RolePermissionVo {

    //当前登录系统的用户的菜单数据
    List<SysMenu> listmenu=new ArrayList<>();
    //角色原来分配的菜单数据
    private  Object[] checkList;
}
