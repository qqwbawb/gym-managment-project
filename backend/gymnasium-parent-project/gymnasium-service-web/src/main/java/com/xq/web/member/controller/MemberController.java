package com.xq.web.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.member.entity.JoinParam;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.PageParam;
import com.xq.web.member.entity.RechargeParam;
import com.xq.web.member.service.MemberService;
import com.xq.web.member_card.entity.MemberCard;
import com.xq.web.member_card.service.MemberCardService;
import com.xq.web.member_role.entity.MemberRole;
import com.xq.web.member_role.service.MemberRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public ResultVo add(@RequestBody Member member){
        //判断会员卡号是否重复
        QueryWrapper<Member> query = new QueryWrapper<>();
        query.lambda().eq(Member::getUsername,member.getUsername());
        Member one = memberService.getOne(query);
        if(one!=null){
            return ResultUtils.error("会员卡号被占用");
        }
        member.setPassword(DigestUtils.md5DigestAsHex(member.getPassword().getBytes()));
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
        IPage<Member> list = memberService.page(page, query);
        return ResultUtils.success("查询成功",list);
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
}
