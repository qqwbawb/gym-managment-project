package com.xq.web.sys_role.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import com.xq.web.sys_role.entity.RoleParm;
import com.xq.web.sys_role.entity.SysRole;
import com.xq.web.sys_role.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
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
}
