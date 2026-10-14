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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
        //余额(money)与会员价格(price)只允许通过充值/办卡等专项接口变更，
        //编辑接口一律不得触碰，防止被恶意构造请求或前端数据覆盖余额
        member.setMoney(null);
        member.setPrice(null);
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
    @Transactional(rollbackFor = Exception.class)
    public void joinApply(JoinParam joinParam) throws ParseException {
        Member select = this.baseMapper.selectById(joinParam.getMemberId());
        if (select == null) {
            throw new RuntimeException("会员不存在");
        }
        //更新会员信息
        MemberCard card = memberCardMapper.selectById(joinParam.getCardId());
        if (card == null) {
            throw new RuntimeException("会员卡类型不存在");
        }

        //办卡费用：从会员余额原子扣减。
        //判断（余额 >= 卡价）与扣减在同一条 SQL 内完成（where money >= ?），
        //由 InnoDB 行锁保证原子性，避免「先查后改」的并发窗口导致余额被扣成负数。
        BigDecimal price = card.getPrice() == null ? BigDecimal.ZERO : card.getPrice();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            RechargeParam deductParam = new RechargeParam();
            deductParam.setMemberId(joinParam.getMemberId());
            deductParam.setMoney(price);
            int affected = this.baseMapper.subMoney(deductParam);
            if (affected == 0) {
                //余额不足或会员不存在：抛异常让整个事务回滚（有效期、办卡明细都不会写入）
                throw new RuntimeException("余额不足，办卡失败");
            }
        }

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
    @Transactional(rollbackFor = Exception.class)
    public void recharge(RechargeParam rechargeParam) {
        if (rechargeParam.getMemberId() == null || rechargeParam.getMoney() == null
                || rechargeParam.getMoney().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额不合法");
        }
        //生成充值明细
        MemberRecharge recharge = new MemberRecharge();
        recharge.setMemberId(rechargeParam.getMemberId());
        recharge.setMoney(rechargeParam.getMoney());
        recharge.setCreateTime(new Date());
        //记录操作人：从登录态取，保证充值流水可审计（"谁给我充的值"）
        recharge.setCreateUser(currentOperator());
        boolean save = memberRechargeService.save(recharge);
        if (!save) {
            throw new RuntimeException("充值失败");
        }
        //会员账户金额更新（SQL 原子自增，并发安全）
        int affected = this.baseMapper.addMoney(rechargeParam);
        if (affected == 0) {
            throw new RuntimeException("充值失败：会员不存在");
        }
    }

    /**
     * 从 Spring Security 登录态中取当前操作人标识。
     * 不依赖前端传入的 userId，避免被伪造。
     */
    private String currentOperator() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails details = (UserDetails) authentication.getPrincipal();
                return details.getUsername();
            }
        } catch (Exception ignored) {
            // 取不到登录态时不影响主流程，留空即可
        }
        return null;
    }

    //根据会员名称查询会员信息
    @Override
    public Member loadUser(String username) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Member::getUsername,username);
        return this.baseMapper.selectOne(queryWrapper);
    }
}
