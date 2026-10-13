package com.xq.web.home.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.equipment.service.MaterialService;
import com.xq.web.goods_order.service.GoodsOrderService;
import com.xq.web.home.entity.Echart;
import com.xq.web.home.entity.EchartItem;
import com.xq.web.home.entity.ResetPassword;
import com.xq.web.home.entity.TotalCount;
import com.xq.web.member.entity.Member;
import com.xq.web.member.service.MemberService;
import com.xq.web.suggest.entity.Suggest;
import com.xq.web.suggest.service.SuggestService;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequestMapping("/api/home")
@RestController
public class HomeController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private GoodsOrderService goodsOrderService;

    //统计总数
    @GetMapping("getTotal")
    public ResultVo getTotal(){
        TotalCount totalCount=new TotalCount();
        totalCount.setMemberCount(memberService.count());
        totalCount.setOrderCount(goodsOrderService.count());
        totalCount.setUserCount(sysUserService.count());
        totalCount.setMaterCount(materialService.count());
        return ResultUtils.success("查询成功",totalCount);
    }

    @Autowired
    private SuggestService suggestService;

    //查询反馈列表
    @GetMapping("/getSuggestList")
    public ResultVo getSuggestList(){
        QueryWrapper<Suggest>  queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(Suggest::getDateTime).last("limit 3");
        List<Suggest> suggestList = suggestService.list(queryWrapper);
        return ResultUtils.success("查询成功",suggestList);
    }

    //查询热销商品
    @GetMapping("/getHotGoods")
    public ResultVo getHotGoods(){
        List<EchartItem> echartItems = goodsOrderService.hotGoods();
        Echart echart=new Echart();
        if(echartItems!=null&& !echartItems.isEmpty()){
            for(EchartItem echartItem:echartItems){
                echart.getNames().add(echartItem.getName());
                echart.getValues().add(echartItem.getValue());
            }
        }
        return ResultUtils.success("查询成功",echart);
    }
    //查询热销卡
    @GetMapping("/getHotCards")
    public ResultVo getHotCards(){
        List<EchartItem> echartItems = goodsOrderService.hotCards();
        return ResultUtils.success("查询成功",echartItems);
    }
    //查询热销课程
    @GetMapping("/getHotCourse")
    public ResultVo getHotCourse(){
        List<EchartItem> echartItems = goodsOrderService.hotCourse();
        return ResultUtils.success("查询成功",echartItems);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    //重置密码
    @PostMapping("/resetPassword")
    public ResultVo resetPassword(@RequestBody ResetPassword resetPassword){
        if(resetPassword.getUserType().equals("1")){
            Member member=new Member();
            member.setMemberId(resetPassword.getUserId());
            member.setPassword(passwordEncoder.encode("123456"));
            memberService.updateById(member);
            return ResultUtils.success("密码重置成功");
        } else if (resetPassword.getUserType().equals("2")) {
            SysUser user=new SysUser();
            user.setUserId(resetPassword.getUserId());
            user.setPassword(passwordEncoder.encode("123456"));
            sysUserService.updateById(user);
            return ResultUtils.success("密码重置成功");
        }else{
            return ResultUtils.error("用户类型错误");
        }
    }

    //修改密码
    @PostMapping("/updatePassword")
    public ResultVo updatePassword(@RequestBody ResetPassword resetPassword){
        if(resetPassword.getUserType().equals("1")){
            Member one= memberService.getById(resetPassword.getUserId());
            if(!passwordEncoder.matches(resetPassword.getOldPassword(),one.getPassword())){
                return ResultUtils.error("原密码不正确");
            }
            Member member=new Member();
            member.setMemberId(resetPassword.getUserId());
            member.setPassword(passwordEncoder.encode(resetPassword.getPassword()));
            memberService.updateById(member);
            return ResultUtils.success("密码修改成功");
        } else if (resetPassword.getUserType().equals("2")) {
            SysUser one= sysUserService.getById(resetPassword.getUserId());
            if(!passwordEncoder.matches(resetPassword.getOldPassword(),one.getPassword())){
                return ResultUtils.error("原密码不正确");
            }
            SysUser user=new SysUser();
            user.setUserId(resetPassword.getUserId());
            user.setPassword(passwordEncoder.encode(resetPassword.getPassword()));
            sysUserService.updateById(user);
            return ResultUtils.success("密码修改成功");
        }else{
            return ResultUtils.error("用户类型错误");
        }
    }

    //退出登录
    @PostMapping("/loginOut")
    public ResultVo loginOut(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication!=null){
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ResultUtils.success("退出登录成功");
    }
}
