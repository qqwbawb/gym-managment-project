package com.xq.web.member_recharge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member_recharge.entity.MemberRecharge;
import com.xq.web.member_recharge.mapper.MemberRechargeMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberRechargeServiceImpl extends ServiceImpl<MemberRechargeMapper, MemberRecharge> implements MemberRechargeService {
}
