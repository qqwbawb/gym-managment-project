package com.xq.web.login.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.xq.jwt.JwtUtils;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.login.entity.InfoParam;
import com.xq.web.login.entity.LoginParam;
import com.xq.web.login.entity.LoginResult;
import com.xq.web.login.entity.UserInfo;
import com.xq.web.member.entity.Member;
import com.xq.web.member.service.MemberService;
import com.xq.web.sys_menu.entity.MakeMenuTree;
import com.xq.web.sys_menu.entity.RouterVo;
import com.xq.web.sys_menu.entity.SysMenu;
import com.xq.web.sys_menu.service.SysMenuService;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    //生成验证码
    @PostMapping("/image")
    public ResultVo imageCode(HttpServletRequest request) throws IOException {
        //获取验证码字符
        String text = defaultKaptcha.createText();
        //将验证码存到session
        HttpSession session = request.getSession();
        session.setAttribute("code", text);
        //生成图片
        BufferedImage image = defaultKaptcha.createImage(text);
        ByteArrayOutputStream os = null;
        try{
            os = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", os);
            BASE64Encoder encoder = new BASE64Encoder();
            String base64 = encoder.encode(os.toByteArray());
            String captcha = "data:image/jpeg;base64," + base64.replaceAll("\r\n", "");
            return new ResultVo<>("生成成功",200,captcha);
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(os != null){
                os.close();
            }
        }
        return null;
    }

    @Autowired
    private MemberService memberService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResultVo login(HttpServletRequest request, @RequestBody LoginParam  loginParam){
        HttpSession session = request.getSession();
        String code=(String) session.getAttribute("code");
        //验证验证码
        if(code == null || !code.equalsIgnoreCase(loginParam.getCode())){
            return ResultUtils.error("验证码输入错误");
        }
        String password = DigestUtils.md5DigestAsHex(loginParam.getPassword().getBytes());

        //判断用户类型
        if(loginParam.getUserType().equals("1")){//会员
            //构造查询条件
            QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Member::getUsername, loginParam.getUsername())
                    .eq(Member::getPassword,password);
            Member one = memberService.getOne(queryWrapper);
            if(one==null){
                return ResultUtils.error("用户名或密码输入错误");
            }
            //生成token
            Map<String,String> map = new HashMap<>();
            map.put("userId",Long.toString(one.getMemberId()));
            map.put("userName",one.getUsername());
            String token = jwtUtils.generateToken(map);
            //返回登录成功的信息
            LoginResult result = new LoginResult();
            result.setToken(token);
            result.setUserId(one.getMemberId());
            result.setUserName(one.getName());
            return ResultUtils.success("登录成功",result);
        }else if(loginParam.getUserType().equals("2")){//员工
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SysUser::getUsername, loginParam.getUsername())
                    .eq(SysUser::getPassword,password);
            SysUser one = sysUserService.getOne(queryWrapper);
            if(one==null){
                return ResultUtils.error("用户名或密码输入错误");
            }
            //生成token
            Map<String,String> map = new HashMap<>();
            map.put("userId",Long.toString(one.getUserId()));
            map.put("userName",one.getUsername());
            String token = jwtUtils.generateToken(map);
            //返回登录成功的信息
            LoginResult result = new LoginResult();
            result.setToken(token);
            result.setUserId(one.getUserId());
            result.setUserName(one.getUsername());
            return ResultUtils.success("登录成功",result);
        }else{
            return ResultUtils.error("用户类型错误");
        }
    }

    @Autowired
    private SysMenuService sysMenuService;

    //查询用户信息
    @GetMapping("/getInfo")
    public ResultVo getInfo(InfoParam infoParam){
        UserInfo userInfo = new UserInfo();
        if(infoParam.getUserType().equals("1")){//会员
            //根据会员id查询对应的权限字段
            List<SysMenu> menuList = sysMenuService.getMenuByMemberId(infoParam.getUserId());
            //获取menu中的code字段
            List<String> collect = Optional.ofNullable(menuList).orElse(new ArrayList<>())
                    .stream()
                    .map(item -> item.getCode())
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
            //转换为数组
            String[] strings=collect.toArray(new String[collect.size()]);
            //查询会员信息
            Member member =memberService.getById(infoParam.getUserId());
            //设置返回信息
            userInfo.setUserId(member.getMemberId());
            userInfo.setName(member.getName());
            userInfo.setPermissions(strings);
            return ResultUtils.success("查询成功",userInfo);
        }else if(infoParam.getUserType().equals("2")){//员工
            SysUser sysUser = sysUserService.getById(infoParam.getUserId());
            List<SysMenu> menuList = null;
            if(StringUtils.isNotEmpty(sysUser.getIsAdmin())&&sysUser.getIsAdmin().equals("1")){//管理员
                menuList = sysMenuService.list();
            }else{
                menuList = sysMenuService.getMenuByUserId(sysUser.getUserId());
            }
            List<String> collect = Optional.ofNullable(menuList).orElse(new ArrayList<>())
                    .stream()
                    .map(item -> item.getCode())
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
            String[] strings=collect.toArray(new String[collect.size()]);
            //设置返回信息
            userInfo.setUserId(sysUser.getUserId());
            userInfo.setName(sysUser.getNickName());
            userInfo.setPermissions(strings);
            return ResultUtils.success("查询成功",userInfo);
        }else{
            return ResultUtils.error("用户类型错误");
        }
    }

    @GetMapping("/getMenuList")
    public ResultVo getMenuList(InfoParam infoParam){
        if(infoParam.getUserType().equals("1")){//会员
            List<SysMenu> menuList = sysMenuService.getMenuByMemberId(infoParam.getUserId());
            //获取菜单信息
            List<SysMenu> collect = Optional.ofNullable(menuList).orElse(new ArrayList<>())
                    .stream()
                    .filter(item -> item != null && !item.getType().equals("2"))
                    .collect(Collectors.toList());
            List<RouterVo> routerVos = MakeMenuTree.makeRouter(collect, 0L);
            return ResultUtils.success("查询成功",routerVos);
        }else if(infoParam.getUserType().equals("2")){//员工
            SysUser sysUser=sysUserService.getById(infoParam.getUserId());
            List<SysMenu> menuList = null;
            if(StringUtils.isNotEmpty(sysUser.getIsAdmin())&&sysUser.getIsAdmin().equals("1")){
                menuList = sysMenuService.list();
            }else{
                menuList = sysMenuService.getMenuByUserId(sysUser.getUserId());
            }
            //获取菜单信息
            List<SysMenu> collect = Optional.ofNullable(menuList).orElse(new ArrayList<>())
                    .stream()
                    .filter(item -> item != null && !item.getType().equals("2"))
                    .collect(Collectors.toList());
            List<RouterVo> routerVos = MakeMenuTree.makeRouter(collect, 0L);
            return ResultUtils.success("查询成功",routerVos);
        }else {
            return ResultUtils.error("用户类型错误");
        }
    }
}
