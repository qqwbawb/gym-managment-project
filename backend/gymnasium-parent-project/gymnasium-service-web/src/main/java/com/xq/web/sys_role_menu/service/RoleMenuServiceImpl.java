package com.xq.web.sys_role_menu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.sys_role_menu.entity.RoleMenu;
import com.xq.web.sys_role_menu.entity.SaveMenuParam;
import com.xq.web.sys_role_menu.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {
    @Override
    @Transactional
    public void saveMenu(SaveMenuParam param) {
        //删除原来的关联关系
        QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoleMenu::getRoleId, param.getRoleId());
        this.baseMapper.delete(queryWrapper);
        //重新保存
        this.baseMapper.saveRoleMenu(param.getRoleId(),param.getList());
    }
}
