package com.xq.web.sys_user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import com.xq.web.sys_role.entity.SelectType;
import com.xq.web.sys_user.entity.PageParam;
import com.xq.web.sys_user.entity.SysUser;
import com.xq.web.sys_user.service.SysUserService;
import com.xq.web.sys_user_role.entity.SysUserRole;
import com.xq.web.sys_user_role.service.SysUserRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    //新增员工
    @PostMapping
    public ResultVo addUser(@RequestBody SysUser sysUser){
        //判断用户信息是否存在
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.lambda().eq(SysUser::getUsername,sysUser.getUsername());
        SysUser one=sysUserService.getOne(query);
        if(one!=null){
            return ResultUtils.error("用户已存在");
        }
        //密码加密
        if(StringUtils.isNotBlank(sysUser.getPassword())){
            sysUser.setPassword(DigestUtils.md5DigestAsHex(sysUser.getPassword().getBytes()));
        }
        sysUser.setIdAdmin("0");
        sysUser.setCreateTime(new Date());
        //存到数据库中
        boolean save = sysUserService.save(sysUser);
        if(save){
            return  ResultUtils.success("新增用户成功");
        }
        return ResultUtils.error("新增用户失败");
    }

    //编辑员工
    @PutMapping
    public ResultVo editUser(@RequestBody SysUser sysUser){
        //判断用户信息是否存在
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.lambda().eq(SysUser::getUsername,sysUser.getUsername());
        SysUser one=sysUserService.getOne(query);
        if(one!=null&&one.getUserId()!=sysUser.getUserId()){
            return ResultUtils.error("账户已被占用");
        }
        //密码加密
        if(StringUtils.isNotBlank(sysUser.getPassword())){
            sysUser.setPassword(DigestUtils.md5DigestAsHex(sysUser.getPassword().getBytes()));
        }
        sysUser.setUpdateTime(new Date());
        //存到数据库中
        boolean save = sysUserService.updateById(sysUser);
        if(save){
            return  ResultUtils.success("编辑用户成功");
        }
        return ResultUtils.error("编辑用户失败");
    }

    //删除用户
    @DeleteMapping("/{userId}")
    public ResultVo deleteUser(@PathVariable("userId") Long userId){
        boolean remove = sysUserService.removeById(userId);
        if(remove){
            return ResultUtils.success("删除用户成功");
        }
        return ResultUtils.error("删除用户失败");
    }

    //查询用户列表
    @GetMapping("/list")
    public ResultVo getList(PageParam param){
        IPage<SysUser> list = sysUserService.list(param);
        //查询不了密码
        list.getRecords().stream().forEach(item->{
            item.setPassword("");
        });
        return ResultUtils.success("用户查询成功",list);
    }

    //根据用户id查询角色id
    @GetMapping("/role")
    public ResultVo getRole(Long userId){
        QueryWrapper<SysUserRole> query = new QueryWrapper<>();
        query.lambda().eq(SysUserRole::getUserId,userId);
        SysUserRole one=sysUserRoleService.getOne(query);
        return ResultUtils.success("查询成功",one);
    }

    //重置密码
    @PutMapping("/resetPwd")
    public ResultVo resetPwd(@RequestBody SysUser sysUser){
        //重置密码
        sysUser.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        sysUser.setUpdateTime(new Date());

        boolean save = sysUserService.updateById(sysUser);
        if(save){
            return  ResultUtils.success("重置密码成功");
        }
        return ResultUtils.error("重置密码失败");
    }

    //查询课程教练
    @GetMapping("/getTeacher")
    public ResultVo getTeacher(){
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.lambda().eq(SysUser::getUserType,2);
        List<SysUser> list = sysUserService.list(query);
        //组装数据
        List<SelectType> selectTypeList = new ArrayList<>();
        if(list!=null&&list.size()>0){
            list.stream().forEach(item->{
                SelectType selectType = new SelectType();
                selectType.setLabel(item.getNickName());
                selectType.setValue(item.getRoleId());
                selectTypeList.add(selectType);
            });
        }
        return ResultUtils.success("查询成功",selectTypeList);
    }
}
