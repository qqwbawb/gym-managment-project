package com.xq.web.sys_role.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.sys_role.entity.RoleParm;
import com.xq.web.sys_role.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> list(RoleParm roleParm);
}
