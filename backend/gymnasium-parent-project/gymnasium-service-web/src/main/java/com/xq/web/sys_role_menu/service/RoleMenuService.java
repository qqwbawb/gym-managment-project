package com.xq.web.sys_role_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.sys_role_menu.entity.RoleMenu;
import com.xq.web.sys_role_menu.entity.SaveMenuParam;

public interface RoleMenuService extends IService<RoleMenu> {
    //保存角色权限
    void saveMenu(SaveMenuParam param);
}
