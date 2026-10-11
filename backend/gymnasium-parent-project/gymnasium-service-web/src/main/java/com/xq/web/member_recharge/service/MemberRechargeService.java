package com.xq.web.member_recharge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member.entity.RechargeParamList;
import com.xq.web.member_recharge.entity.MemberRecharge;

public interface MemberRechargeService extends IService<MemberRecharge> {
    IPage<MemberRecharge> getRechargeList(RechargeParamList rechargeParamList);
    IPage<MemberRecharge>  getRechargeByMemberId(RechargeParamList rechargeParamList);
}
