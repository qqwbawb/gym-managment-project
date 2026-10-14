package com.xq.web.member_course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.web.member_course.entity.MemberCourse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberCourseMapper extends BaseMapper<MemberCourse> {

    /**
     * 课程订单分页查询：关联 member 表带出报名会员的姓名与卡号。
     *
     * @param page       分页对象
     * @param courseName 课程名称（模糊搜索，可为空）
     * @return 课程报名记录分页
     */
    IPage<MemberCourse> selectCourseOrderPage(Page<MemberCourse> page, @Param("courseName") String courseName);

    /**
     * 查询报名某门课程的全部学员（用于导出学生名单，不分页）。
     *
     * @param courseId 课程 id
     * @return 报名该课程的学员列表（含会员姓名 / 卡号 / 电话）
     */
    List<MemberCourse> selectCourseStudents(@Param("courseId") Long courseId);
}
