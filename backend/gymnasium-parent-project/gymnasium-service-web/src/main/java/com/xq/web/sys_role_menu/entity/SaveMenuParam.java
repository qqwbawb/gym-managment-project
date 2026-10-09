package com.xq.web.sys_role_menu.entity;

import lombok.Data;

import java.util.List;

/**
 * 用户发送分配菜单权限的请求，携带参数的封装
 */
@Data
public class SaveMenuParam {
    private Long roleId;//角色id

    private List<Long> list;//复选菜单id的集合
}
