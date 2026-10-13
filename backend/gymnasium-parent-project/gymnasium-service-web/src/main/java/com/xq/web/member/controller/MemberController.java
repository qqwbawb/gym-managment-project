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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

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
    public ResultVo joinApply(@RequestBody JoinParam joinParam)throws ParseException {
        memberService.joinApply(joinParam);
        return ResultUtils.success("办卡成功");
    }

    //充值
    @PostMapping("/recharge")
    public ResultVo recharge(@RequestBody RechargeParam rechargeParam){
        memberService.recharge(rechargeParam);
        return  ResultUtils.success("充值成功");
    }

    @Autowired
    private MemberRechargeService memberRechargeService;

    //充值查询
    @GetMapping("/getMyRecharge")
    public ResultVo getMyRecharge(RechargeParamList rechargeParamList){
        //判断当前用户是会员还是员工
        if(rechargeParamList.getUserType().equals("1")){
            IPage<MemberRecharge> list = memberRechargeService.getRechargeByMemberId(rechargeParamList);
            return ResultUtils.success("查询成功",list);
        } else if (rechargeParamList.getUserType().equals("2")) {
            IPage<MemberRecharge> list = memberRechargeService.getRechargeList(rechargeParamList);
            return ResultUtils.success("查询成功",list);
        }else {
            return ResultUtils.error("用户类型错误");
        }
    }
}
