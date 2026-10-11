package com.xq.web.member_recharge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xq.web.member_recharge.entity.MemberRecharge;
import org.apache.ibatis.annotations.Param;

public interface MemberRechargeMapper extends BaseMapper<MemberRecharge> {

    IPage<MemberRecharge> getRechargeList(IPage<MemberRecharge> page);

    IPage<MemberRecharge> getRechargeByMemberId(IPage<MemberRecharge> page,@Param("memberId") Long memberId);
}
