package com.xq.web.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member.entity.JoinParam;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.RechargeParam;

import java.text.ParseException;

public interface MemberService extends IService<Member> {

    void addMember(Member member);
    void editMember(Member member);
    void deleteMember(Long memberId);

    void joinApply(JoinParam joinParam)throws ParseException;

    void recharge(RechargeParam rechargeParam);
}
