package com.xq.web.ai.service;

import com.xq.web.member.entity.Member;
import com.xq.web.sys_user.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前登录会员解析器。
 *
 * 为什么要单独抽出来：
 *   AI 模块所有接口都必须以「登录态」为准来确定 memberId。
 *   如果接受前端传入的 memberId，会员只要改成别人的 id 就能读写他人画像、
 *   对话与训练记录 —— 这是典型的水平越权（IDOR）。
 *   项目里 MemberController#getMyRecharge 已经因为同样问题修过一次，这里保持一致做法。
 */
@Component
public class CurrentMemberResolver {

    /**
     * 取当前登录会员。
     *
     * @return Member；若当前登录者不是会员（例如员工账号），返回 null
     */
    public Member currentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Member) {
            return (Member) principal;
        }
        return null;
    }

    /**
     * 取当前登录会员 id，非会员登录返回 null。
     */
    public Integer currentMemberId() {
        Member member = currentMember();
        return member == null ? null : member.getMemberId().intValue();
    }

    /**
     * 校验当前登录者确实是会员，否则抛业务异常。
     * 用于 AI 功能入口：本模块只对会员开放。
     */
    public Integer requireMemberId() {
        Member member = currentMember();
        if (member == null) {
            throw new IllegalStateException("AI 健身教练仅对会员开放，请使用会员账号登录");
        }
        return member.getMemberId().intValue();
    }

    /**
     * 当前登录者是否为员工（用于区分提示文案）。
     */
    public boolean isStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SysUser;
    }
}
