package com.xq.web.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.RechargeParam;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper extends BaseMapper<Member> {

    int addMoney(@Param("param") RechargeParam param);

    /**
     * 余额原子扣减。
     * SQL 带 `and money >= #{param.money}` 条件，判断与扣减在一条 SQL 内完成。
     *
     * @param param 会员 id + 扣减金额
     * @return 影响行数：1 = 扣减成功；0 = 余额不足或会员不存在
     */
    int subMoney(@Param("param") RechargeParam param);
}
