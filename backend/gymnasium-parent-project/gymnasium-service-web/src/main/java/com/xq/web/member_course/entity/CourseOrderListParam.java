package com.xq.web.member_course.entity;

import lombok.Data;

/**
 * 课程订单分页查询参数
 */
@Data
public class CourseOrderListParam {

    /**
     * 课程名称（模糊搜索）
     */
    private String courseName;

    private Integer currentPage;

    private Integer pageSize;

    private Long total;
}
