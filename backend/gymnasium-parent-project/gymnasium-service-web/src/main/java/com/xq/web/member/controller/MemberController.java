package com.xq.web.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.member.entity.*;
import com.xq.web.member.service.MemberService;
import com.xq.web.member_card.entity.MemberCard;
import com.xq.web.member_card.service.MemberCardService;
import com.xq.web.member_recharge.entity.MemberRecharge;
import com.xq.web.member_recharge.service.MemberRechargeService;
import com.xq.web.member_role.entity.MemberRole;
import com.xq.web.member_role.service.MemberRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import com.xq.web.sys_user.entity.SysUser;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRoleService memberRoleService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasAuthority('sys:memberList:add')")
    public ResultVo add(@RequestBody Member member){
        //判断会员卡号是否重复
        QueryWrapper<Member> query = new QueryWrapper<>();
        query.lambda().eq(Member::getUsername,member.getUsername());
        Member one = memberService.getOne(query);
        if(one!=null){
            return ResultUtils.error("会员卡号被占用");
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberService.addMember(member);
        return ResultUtils.success("会员信息新增成功");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sys:memberList:edit')")
    public ResultVo edit(@RequestBody Member member){
        QueryWrapper<Member> query = new QueryWrapper<>();
        query.lambda().eq(Member::getUsername,member.getUsername());
        Member one = memberService.getOne(query);
        if(one!=null&&!one.getMemberId().equals(member.getMemberId())){
            return ResultUtils.error("会员卡号被占用");
        }
        memberService.editMember(member);
        return ResultUtils.success("编辑成功");
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAuthority('sys:memberList:delete')")
    public ResultVo delete(@PathVariable("memberId") Long memberId){
        memberService.deleteMember(memberId);
        return ResultUtils.success("删除成功");
    }

    @GetMapping("/list")
    public ResultVo list(PageParam pageParam){
        if(pageParam.getUserType().equals("1")){
            IPage<Member> page=new Page<>(pageParam.getCurrentPage(),pageParam.getPageSize());
            QueryWrapper<Member> query = new QueryWrapper<>();
            if(StringUtils.isNotEmpty(pageParam.getName())){
                query.lambda().like(Member::getName,pageParam.getName());
            }
            if(StringUtils.isNotEmpty(pageParam.getPhone())){
                query.lambda().like(Member::getPhone,pageParam.getPhone());
            }
            if(StringUtils.isNotEmpty(pageParam.getUsername())){
                query.lambda().like(Member::getUsername,pageParam.getUsername());
            }
            query.lambda().eq(Member::getMemberId,pageParam.getUserId());
            //排序
            query.lambda().orderByDesc(Member::getJoinTime);
            IPage<Member> list = memberService.page(page, query);
            return ResultUtils.success("查询成功",list);
        }else {
            IPage<Member> page=new Page<>(pageParam.getCurrentPage(),pageParam.getPageSize());
            QueryWrapper<Member> query = new QueryWrapper<>();
            if(StringUtils.isNotEmpty(pageParam.getName())){
                query.lambda().like(Member::getName,pageParam.getName());
            }
            if(StringUtils.isNotEmpty(pageParam.getPhone())){
                query.lambda().like(Member::getPhone,pageParam.getPhone());
            }
            if(StringUtils.isNotEmpty(pageParam.getUsername())){
                query.lambda().like(Member::getUsername,pageParam.getUsername());
            }
            query.lambda().orderByDesc(Member::getJoinTime);
            IPage<Member> list = memberService.page(page, query);
            return ResultUtils.success("查询成功",list);
        }
    }

    //根据会员id查询角色id
    @GetMapping("/getRoleByMemberId")
    public ResultVo getRoleByMemberId(Long memberId){
        QueryWrapper<MemberRole> query = new QueryWrapper<>();
        query.lambda().eq(MemberRole::getMemberId,memberId);
        MemberRole one = memberRoleService.getOne(query);
        return ResultUtils.success("查询成功",one);
    }

    @Autowired
    private MemberCardService memberCardService;

    //查询会员卡列表
    @GetMapping("/getCardList")
    public ResultVo getCardList(){
        QueryWrapper<MemberCard>  query = new QueryWrapper<>();
        query.lambda().eq(MemberCard::getStatus,"1");
        List<MemberCard> list = memberCardService.list(query);
        return ResultUtils.success("查询成功",list);
    }

    //办卡提交
    @PostMapping("/joinApply")
    @PreAuthorize("hasAuthority('sys:memberList:setCard')")
    public ResultVo joinApply(@RequestBody JoinParam joinParam)throws ParseException {
        memberService.joinApply(joinParam);
        return ResultUtils.success("办卡成功");
    }

    //充值
    @PostMapping("/recharge")
    @PreAuthorize("hasAuthority('sys:memberList:rechart')")
    public ResultVo recharge(@RequestBody RechargeParam rechargeParam){
        memberService.recharge(rechargeParam);
        return  ResultUtils.success("充值成功");
    }

    @Autowired
    private MemberRechargeService memberRechargeService;

    //充值查询
    @GetMapping("/getMyRecharge")
    public ResultVo getMyRecharge(RechargeParamList rechargeParamList){
        //从登录态（SecurityContext）解析当前用户身份，而不是信任前端传入的 userType / memberId。
        //原因：userType 由前端提交时可被篡改（例如会员传 userType=2 就能查全部充值记录），
        //属于越权访问（水平/垂直越权），必须以后端登录态为准。
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResultUtils.error("未获取到登录信息");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        //会员：只能查自己的充值记录；员工：可查全部
        if (userDetails instanceof Member) {
            Member loginMember = (Member) userDetails;
            //以登录态中的会员 id 覆盖前端传入值，防止越权查询他人记录
            rechargeParamList.setMemberId(loginMember.getMemberId());
            rechargeParamList.setUserType("1");
            IPage<MemberRecharge> list = memberRechargeService.getRechargeByMemberId(rechargeParamList);
            return ResultUtils.success("查询成功", list);
        } else if (userDetails instanceof SysUser) {
            rechargeParamList.setUserType("2");
            IPage<MemberRecharge> list = memberRechargeService.getRechargeList(rechargeParamList);
            return ResultUtils.success("查询成功", list);
        } else {
            return ResultUtils.error("用户类型错误");
        }
    }
}
