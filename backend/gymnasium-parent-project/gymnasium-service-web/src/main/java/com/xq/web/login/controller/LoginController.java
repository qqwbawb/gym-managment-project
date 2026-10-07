package com.xq.web.login.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.xq.jwt.JwtUtils;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.login.entity.LoginParam;
import com.xq.web.login.entity.LoginResult;
import com.xq.web.member.entity.Member;
import com.xq.web.member.service.MemberService;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            ResultVo result=new ResultVo<>("生成成功",200,captcha);
            return result;
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
}
