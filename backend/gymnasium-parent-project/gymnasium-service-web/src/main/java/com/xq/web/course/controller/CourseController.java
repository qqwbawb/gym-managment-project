package com.xq.web.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.course.entity.Course;
import com.xq.web.course.entity.CourseList;
import com.xq.web.course.service.CourseService;
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
}
