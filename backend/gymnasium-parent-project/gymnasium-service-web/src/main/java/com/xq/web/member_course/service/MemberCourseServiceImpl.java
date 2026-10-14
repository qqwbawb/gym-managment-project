package com.xq.web.member_course.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.course.entity.Course;
import com.xq.web.course.service.CourseService;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.RechargeParam;
import com.xq.web.member.mapper.MemberMapper;
import com.xq.web.member.service.MemberService;
import com.xq.web.member_course.entity.MemberCourse;
import com.xq.web.member_course.mapper.MemberCourseMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberCourseServiceImpl extends ServiceImpl<MemberCourseMapper, MemberCourse> implements MemberCourseService {

    @Autowired
    private CourseService courseService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 选课报名。
     *
     * 事务边界：整个方法在一个事务内，任一步骤失败全部回滚。
     * 并发控制：
     *   ① 重复报名 —— 查询校验（业务层面），配合数据库唯一索引更稳妥；
     *   ② 余额扣减 —— SQL 原子自减 `money = money - ? where member_id = ? and money >= ?`，
     *      影响行数为 0 说明余额不足，抛异常回滚，从根上消除「先查后改」的并发窗口。
     *
     * 注意：这里不再依赖 Controller 层预先判断余额。即使并发导致余额在判断后被扣走，
     *      原子扣款的 where 条件也会兜住，不会出现负余额或「报名成功却没扣钱」。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinCourse(MemberCourse memberCourse) {
        Long courseId = memberCourse.getCourseId();
        Long memberId = memberCourse.getMemberId();
        if (courseId == null || memberId == null) {
            throw new RuntimeException("选课参数不合法");
        }

        //① 重复报名校验
        QueryWrapper<MemberCourse> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberCourse::getCourseId, courseId)
                .eq(MemberCourse::getMemberId, memberId);
        MemberCourse exists = this.getOne(queryWrapper);
        if (exists != null) {
            throw new RuntimeException("已经报名过了");
        }

        //② 查询课程与会员
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        //③ 余额预校验：给出友好提示（并发场景下真正的保障是下面的原子扣款）
        if (member.getMoney() == null || member.getMoney().compareTo(course.getCoursePrice()) < 0) {
            throw new RuntimeException("余额不足");
        }

        //④ 原子扣款：where 带 money >= ? 条件，判断与扣减在一条 SQL 内完成
        RechargeParam param = new RechargeParam();
        param.setMemberId(memberId);
        param.setMoney(course.getCoursePrice());
        int affected = memberMapper.subMoney(param);
        if (affected == 0) {
            //并发场景：预校验通过但扣款时余额已被其它事务扣走
            throw new RuntimeException("余额不足");
        }

        //⑤ 写入报名记录（与扣款同属一个事务）
        BeanUtils.copyProperties(course, memberCourse);
        this.baseMapper.insert(memberCourse);
    }
}
