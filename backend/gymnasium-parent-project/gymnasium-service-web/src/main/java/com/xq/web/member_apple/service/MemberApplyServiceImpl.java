package com.xq.web.member_apple.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member_apple.entity.MemberApply;
import com.xq.web.member_apple.mapper.MemberApplyMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberApplyServiceImpl extends ServiceImpl<MemberApplyMapper, MemberApply> implements MemberApplyService {
}
