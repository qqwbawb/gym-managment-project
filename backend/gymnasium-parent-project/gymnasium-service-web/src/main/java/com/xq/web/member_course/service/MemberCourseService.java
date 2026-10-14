package com.xq.web.member_course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member_course.entity.MemberCourse;

public interface MemberCourseService extends IService<MemberCourse> {

    /**
     * 选课报名（事务版本）
     *
     * 一个事务内完成：重复报名校验 → 余额校验 → 原子扣款 → 写入报名记录。
     * 余额判断与扣款都在 Service 层，且扣款用 SQL 原子操作（where money >= ?），
     * 避免「先查后改」的并发窗口，也避免 Controller 层判断与 Service 层扣款
     * 分处两个事务导致的数据不一致。
     *
     * @param memberCourse 选课参数（会员 id + 课程 id）
     * @throws RuntimeException 重复报名 / 余额不足 / 课程不存在时抛出，触发事务回滚
     */
    void joinCourse(MemberCourse memberCourse);
}
