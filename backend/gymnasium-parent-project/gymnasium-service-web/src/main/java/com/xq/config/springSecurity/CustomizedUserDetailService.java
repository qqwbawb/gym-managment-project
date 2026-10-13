package com.xq.config.springSecurity;

import com.xq.web.member.entity.Member;
import com.xq.web.member.service.MemberService;
import com.xq.web.sys_menu.entity.SysMenu;
import com.xq.web.sys_menu.service.SysMenuService;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义认证管理器
 */
@Component("customizedUserDetailService")
public class CustomizedUserDetailService implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //认证
        int index = s.indexOf(":");
        String username = s.substring(0, index);
        String userType = s.substring(index + 1, s.length());
        if(userType.equals("1")){//会员
            Member user = memberService.loadUser(username);
            //授权
            if(user == null){
                throw new UsernameNotFoundException("用户名或密码错误");
            }
            //把该用户拥有的权限交给SpringSecurity管理
            List<SysMenu> menuList = sysMenuService.getMenuByMemberId(user.getMemberId());
            //获取code字段
            List<String> collect = menuList.stream().map(item -> item.getCode())
                    .filter(item -> item != null && StringUtils.isNotEmpty(item))
                    .collect(Collectors.toList());
            String[] strings = collect.toArray(new String[collect.size()]);
            List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(strings);
            user.setAuthorities(authorityList);
            return user;
        }else if(userType.equals("2")){//员工
            SysUser user = sysUserService.loadUser(username);
            //授权
            if(user == null){
                throw new UsernameNotFoundException("用户名或密码错误");
            }
            List<SysMenu> menuList = null;
            if(StringUtils.isNotEmpty(user.getIsAdmin())&&user.getIsAdmin().equals("1")){
                menuList=sysMenuService.list();
            }else{
                menuList=sysMenuService.getMenuByUserId(user.getUserId());
            }
            //获取code字段
            List<String> collect = menuList.stream().map(item -> item.getCode())
                    .filter(item -> item != null && StringUtils.isNotEmpty(item))
                    .collect(Collectors.toList());
            String[] strings = collect.toArray(new String[collect.size()]);
            List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(strings);
            user.setAuthorities(authorityList);
            return user;
        }else {
            throw new UsernameNotFoundException("用户类型不存在");
        }
    }
}
