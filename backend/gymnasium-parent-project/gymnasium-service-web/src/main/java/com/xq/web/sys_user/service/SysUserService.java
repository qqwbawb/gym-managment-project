package com.xq.web.sys_user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member.entity.Member;
import com.xq.web.sys_user.entity.PageParam;
import com.xq.web.sys_user.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    IPage<SysUser> list(PageParam pageParam);

}
