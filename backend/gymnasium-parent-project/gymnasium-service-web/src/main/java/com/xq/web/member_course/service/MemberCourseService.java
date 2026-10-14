package com.xq.web.member_course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member_course.entity.CourseOrderListParam;
import com.xq.web.member_course.entity.CourseOrderParam;
import com.xq.web.member_course.entity.MemberCourse;

import java.util.List;

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

    /**
     * 课程订单下单（模仿商品订单 /api/order/down 的批量下单）。
     *
     * 为一个会员一次报名多门课程：整个方法在一个事务内，
     * 逐门课程执行「原子扣减会员余额 + 写入报名记录」，
     * 任一门课程余额不足或已报名则抛异常，全部回滚。
     *
     * @param orderParam 会员 id + 课程列表
     * @throws RuntimeException 参数不合法 / 会员不存在 / 已报名 / 余额不足时抛出
     */
    void down(CourseOrderParam orderParam);

    /**
     * 课程订单分页查询（关联会员姓名 / 卡号）。
     *
     * @param listParam 分页与搜索参数
     * @return 课程报名记录分页
     */
    IPage<MemberCourse> getCourseOrderPage(CourseOrderListParam listParam);

    /**
     * 查询报名某门课程的全部学员（导出学生名单用）。
     *
     * @param courseId 课程 id
     * @return 报名该课程的学员列表
     */
    List<MemberCourse> getCourseStudents(Long courseId);
}
