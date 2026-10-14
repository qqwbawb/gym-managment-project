package com.xq.web.member_course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.member_course.entity.CourseOrderListParam;
import com.xq.web.member_course.entity.CourseOrderParam;
import com.xq.web.member_course.entity.MemberCourse;
import com.xq.web.member_course.service.MemberCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程订单（模仿商品订单 /api/order）：
 *   POST /api/courseOrder/down  —— 批量选课下单（原子扣会员余额）
 *   GET  /api/courseOrder/list  —— 课程订单分页列表（含报名会员）
 */
@RestController
@RequestMapping("/api/courseOrder")
public class MemberCourseOrderController {

    @Autowired
    private MemberCourseService memberCourseService;

    /**
     * 课程下单（模仿商品订单 /api/order/down）。
     * 业务逻辑与事务边界都在 Service 层，Controller 只负责接收参数、包装返回结果。
     */
    @PostMapping("/down")
    @PreAuthorize("hasAuthority('sys:courseOrder:index')")
    public ResultVo down(@RequestBody CourseOrderParam orderParam) {
        try {
            memberCourseService.down(orderParam);
            return ResultUtils.success("报名成功");
        } catch (RuntimeException e) {
            //余额不足 / 已报名等业务失败：事务已回滚，返回友好提示
            return ResultUtils.error(e.getMessage());
        }
    }

    /**
     * 课程订单列表（分页 + 课程名称模糊搜索）。
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:courseOrder:index')")
    public ResultVo list(CourseOrderListParam listParam) {
        IPage<MemberCourse> page = memberCourseService.getCourseOrderPage(listParam);
        return ResultUtils.success("查询成功", page);
    }
}
