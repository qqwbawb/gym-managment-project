package com.xq.web.member_role.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member_role.entity.MemberRole;
import com.xq.web.member_role.mapper.MemberRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberRoleServiceImpl extends ServiceImpl<MemberRoleMapper, MemberRole> implements MemberRoleService {
}
