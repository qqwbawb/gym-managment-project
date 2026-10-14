package com.xq.web.member_course.entity;

import lombok.Data;

import java.util.List;

/**
 * 课程订单下单参数（模仿商品订单 OrderParam）
 */
@Data
public class CourseOrderParam {

    /**
     * 报名会员 id（为哪位会员报名，从登录态/前端选择获取）
     */
    private Long memberId;

    /**
     * 选课车：本次报名的课程列表
     */
    private List<CourseOrderItem> courseList;
}
