package com.xq.web.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.RechargeParam;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper extends BaseMapper<Member> {

    int addMoney(@Param("param") RechargeParam param);

    void subMoney(@Param("param") RechargeParam param);
}
