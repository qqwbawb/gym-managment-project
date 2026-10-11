package com.xq.web.member_course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xq.web.member_course.entity.MemberCourse;

public interface MemberCourseService extends IService<MemberCourse> {

    void joinCourse(MemberCourse memberCourse);
}
