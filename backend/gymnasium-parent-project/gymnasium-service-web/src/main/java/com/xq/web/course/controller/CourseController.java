package com.xq.web.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.course.entity.Course;
import com.xq.web.course.entity.CourseList;
import com.xq.web.course.entity.PageParam;
import com.xq.web.course.service.CourseService;
import com.xq.web.member_course.entity.MemberCourse;
import com.xq.web.member_course.service.MemberCourseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAuthority('sys:courseList:add')")
    public ResultVo addCourse(@RequestBody Course course) {
        if(courseService.save(course)) {
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sys:courseList:edit')")
    public ResultVo editCourse(@RequestBody Course course) {
        if(courseService.updateById(course)) {
            return ResultUtils.success("编辑成功");
        }
        return ResultUtils.error("编辑失败");
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAuthority('sys:courseList:delete')")
    public ResultVo deleteCourse(@PathVariable("courseId") Long courseId) {
        if(courseService.removeById(courseId)) {
            return ResultUtils.success("删除成功");
        }
        return ResultUtils.error("删除失败");
    }

    @GetMapping("/list")
    public ResultVo listCourse(CourseList courseList) {
        IPage<Course> page=new Page<>(courseList.getCurrentPage(),courseList.getPageSize());
        QueryWrapper<Course> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotEmpty(courseList.getCourseName())) {
            queryWrapper.lambda().like(Course::getCourseName,courseList.getCourseName());
        }
        if(StringUtils.isNotEmpty(courseList.getTeacherName())) {
            queryWrapper.lambda().like(Course::getTeacherName,courseList.getTeacherName());
        }
        IPage<Course> courses=courseService.page(page,queryWrapper);
        return ResultUtils.success("查询成功",courses);
    }

    @Autowired
    private MemberCourseService memberCourseService;

    /**
     * 选课报名。
     * 业务逻辑与事务边界全部下沉到 MemberCourseService.joinCourse()，
     * Controller 只负责接收参数、包装返回结果。
     */
    @PostMapping("/joinCourse")
    @PreAuthorize("hasAuthority('sys:courseList:join')")
    public ResultVo joinCourse(@RequestBody MemberCourse memberCourse) {
        try {
            memberCourseService.joinCourse(memberCourse);
            return ResultUtils.success("报名成功");
        } catch (RuntimeException e) {
            //重复报名 / 余额不足等业务失败：事务已回滚，返回友好提示
            return ResultUtils.error(e.getMessage());
        }
    }

    //查询我的课程列表
    @GetMapping("/getMyCourseList")
    public ResultVo getMyCourseList(PageParam  pageParam) {
        if(pageParam.getUserType().equals("1")) {
            IPage<MemberCourse> page = new Page<>(pageParam.getCurrentPage(), pageParam.getPageSize());
            QueryWrapper<MemberCourse> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(MemberCourse::getMemberId, pageParam.getUserId());
            IPage<MemberCourse> courses = memberCourseService.page(page, queryWrapper);
            return ResultUtils.success("查询成功", courses);
        }else{
            IPage<Course> page = new Page<>(pageParam.getCurrentPage(), pageParam.getPageSize());
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Course::getTeacherId, pageParam.getUserId());
            IPage<Course> page1 = courseService.page(page, queryWrapper);
            return ResultUtils.success("查询成功", page1);
        }
    }

    /**
     * 查看报名某门课程的学生名单（前端弹抽屉展示，不分页）。
     * 对应菜单「我的课程」下的「导出学生」按钮权限。
     */
    @GetMapping("/getCourseStudents")
    @PreAuthorize("hasAuthority('sys:mycourse:export')")
    public ResultVo getCourseStudents(@RequestParam("courseId") Long courseId) {
        List<MemberCourse> students = memberCourseService.getCourseStudents(courseId);
        return ResultUtils.success("查询成功", students);
    }
}
