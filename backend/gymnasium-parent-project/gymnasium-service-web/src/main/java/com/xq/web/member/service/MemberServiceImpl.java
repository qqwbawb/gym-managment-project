package com.xq.web.member.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member.entity.JoinParam;
import com.xq.web.member.entity.Member;
import com.xq.web.member.entity.RechargeParam;
import com.xq.web.member.mapper.MemberMapper;
import com.xq.web.member_apple.entity.MemberApply;
import com.xq.web.member_apple.mapper.MemberApplyMapper;
import com.xq.web.member_card.entity.MemberCard;
import com.xq.web.member_card.mapper.MemberCardMapper;
import com.xq.web.member_recharge.entity.MemberRecharge;
import com.xq.web.member_recharge.service.MemberRechargeService;
import com.xq.web.member_role.entity.MemberRole;
import com.xq.web.member_role.service.MemberRoleService;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper,Member> implements MemberService {

    @Autowired
    private MemberRoleService memberRoleService;

    @Override
    @Transactional
    public void addMember(Member member) {
        //新增会员
        int insert = this.baseMapper.insert(member);
        //设置会员信息
        if(insert>0){
            MemberRole role = new MemberRole();
            role.setMemberId(member.getMemberId());
            role.setRoleId(member.getRoleId());
            memberRoleService.save(role);
        }
    }

    @Override
    @Transactional
    public void editMember(Member member) {
        //编辑会员本身
        int i = this.baseMapper.updateById(member);
        if(i>0){
            //删除原来的关联信息
            QueryWrapper<MemberRole> query = new QueryWrapper<>();
            query.lambda().eq(MemberRole::getMemberId,member.getMemberId());
            memberRoleService.remove(query);
            //新建新的关联信息
            MemberRole role = new MemberRole();
            role.setMemberId(member.getMemberId());
            role.setRoleId(member.getRoleId());
            memberRoleService.save(role);
        }
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        //删除会员本身的信息
        int i = this.baseMapper.deleteById(memberId);
        if(i>0){
            QueryWrapper<MemberRole> query = new QueryWrapper<>();
            query.lambda().eq(MemberRole::getMemberId,memberId);
            memberRoleService.remove(query);
        }
    }

    @Resource
    private MemberCardMapper memberCardMapper;

    @Resource
    private MemberApplyMapper memberApplyMapper;

    @Autowired
    private MemberRechargeService  memberRechargeService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional
    public void joinApply(JoinParam joinParam) throws ParseException {
        Member select = this.baseMapper.selectById(joinParam.getMemberId());
        //更新会员信息
        MemberCard card = memberCardMapper.selectById(joinParam.getCardId());
        Member member = new Member();
        member.setMemberId(joinParam.getMemberId());
        member.setCardType(card.getTitle());
        member.setCardDay(card.getCardDay());

        String endTime=select.getEndTime();
        Calendar calendar = Calendar.getInstance();
        if(StringUtils.isNotBlank(endTime)){
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(endTime);
            calendar.setTime(date);
            calendar.add(Calendar.DATE, card.getCardDay());
        }else{
            Date date = new Date();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, card.getCardDay());
        }

        member.setEndTime(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        this.baseMapper.updateById(member);
        //插入办卡明细
        MemberApply memberApply = new MemberApply();
        memberApply.setCardDay(card.getCardDay());
        memberApply.setCardType(card.getTitle());
        memberApply.setMemberId(joinParam.getMemberId());
        memberApply.setCreateTime(new Date());
        memberApply.setPrice(card.getPrice());
        memberApplyMapper.insert(memberApply);
    }

    @Override
    @Transactional
    public void recharge(RechargeParam rechargeParam) {
        //SysUser user = sysUserService.getById(rechargeParam.getUserId());
        //生成充值明细
        MemberRecharge recharge = new MemberRecharge();
        recharge.setMemberId(rechargeParam.getMemberId());
        recharge.setMoney(rechargeParam.getMoney());
        recharge.setCreateTime(new Date());
        //recharge.setCreateUser(user.getUsername());
        boolean save = memberRechargeService.save(recharge);
        if(save){
            //会员账户金额更新
            this.baseMapper.addMoney(rechargeParam);
        }
    }
}
