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
import com.xq.web.member.entity.Member;
import com.xq.web.member.service.MemberService;
import com.xq.web.member_course.entity.MemberCourse;
import com.xq.web.member_course.service.MemberCourseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResultVo addCourse(@RequestBody Course course) {
        if(courseService.save(course)) {
            return ResultUtils.success("新增成功");
        }
        return ResultUtils.error("新增失败");
    }

    @PutMapping
    public ResultVo editCourse(@RequestBody Course course) {
        if(courseService.updateById(course)) {
            return ResultUtils.success("编辑成功");
        }
        return ResultUtils.error("编辑失败");
    }

    @DeleteMapping("/{courseId}")
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

    @Autowired
    private MemberService memberService;

    //报名课程
    @PostMapping("/joinCourse")
    public ResultVo joinCourse(@RequestBody MemberCourse memberCourse) {
        //查询是否已经报过该课程
        QueryWrapper<MemberCourse> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberCourse::getCourseId,memberCourse.getCourseId())
                    .eq(MemberCourse::getMemberId,memberCourse.getMemberId());
        MemberCourse one = memberCourseService.getOne(queryWrapper);
        if(one!=null) {
            return ResultUtils.error("已经报名过了");
        }
        //判断余额是否足够
        Course course = courseService.getById(memberCourse.getCourseId());
        Member member = memberService.getById(memberCourse.getMemberId());
        int flag = member.getMoney().compareTo(course.getCoursePrice());
        if(flag==-1) {
            return ResultUtils.error("余额不足");
        }
        memberCourseService.joinCourse(memberCourse);
        return ResultUtils.success("报名成功");
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
}
