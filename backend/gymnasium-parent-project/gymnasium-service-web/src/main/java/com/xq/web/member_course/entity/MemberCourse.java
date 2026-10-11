package com.xq.web.member_course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("member_course")
public class MemberCourse {

    @TableId(type = IdType.AUTO)
    private Long memberCourseId;

    private Long courseId;
    private Long memberId;
    private Long teacherId;
    private String courseName;
    private BigDecimal coursePrice;
    private String courseDetails;
    private String image;
    private Integer courseHour;
    private String teacherName;
}
