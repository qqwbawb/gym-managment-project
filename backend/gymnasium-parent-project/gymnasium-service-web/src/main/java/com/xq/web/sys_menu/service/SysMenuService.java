package com.xq.web.sys_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.sys_menu.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> getParent();
}
