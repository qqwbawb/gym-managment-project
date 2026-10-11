package com.xq.web.member_recharge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member.entity.RechargeParamList;
import com.xq.web.member_recharge.entity.MemberRecharge;
import com.xq.web.member_recharge.mapper.MemberRechargeMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberRechargeServiceImpl extends ServiceImpl<MemberRechargeMapper, MemberRecharge> implements MemberRechargeService {
    @Override
    public IPage<MemberRecharge> getRechargeList(RechargeParamList rechargeParamList) {
        //构造分页对象
        IPage<MemberRecharge> page=new Page<>(rechargeParamList.getCurrentPage(),rechargeParamList.getPageSize());
        return this.baseMapper.getRechargeList(page);
    }

    @Override
    public IPage<MemberRecharge> getRechargeByMemberId(RechargeParamList rechargeParamList) {
        //构造分页对象
        IPage<MemberRecharge> page=new Page<>(rechargeParamList.getCurrentPage(),rechargeParamList.getPageSize());
        return this.baseMapper.getRechargeByMemberId(page,rechargeParamList.getMemberId());
    }
}
