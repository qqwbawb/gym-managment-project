package com.xq.web.sys_menu.entity;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 构造菜单和路由数据的实体类
 */
public class MakeMenuTree {

    //构造菜单树
    public static List<SysMenu> makeTree(List<SysMenu> menus,Long pid){
        List<SysMenu> list=new ArrayList<>();
        Optional.ofNullable(menus).orElse(new ArrayList<>())
                .stream()
                .filter(item-> item!=null&&item.getParentId().equals(pid))
                .forEach(item->{
                    SysMenu sysMenu=new SysMenu();
                    BeanUtils.copyProperties(item,sysMenu);
                    //递归查找下级
                    List<SysMenu> children=makeTree(menus,item.getMenuId());
                    sysMenu.setChildren(children);
                    list.add(sysMenu);
                });
        return list;
    }

    //构造路由数据
    public static List<RouterVo> makeRouter(List<SysMenu> menus,Long pid){
        List<RouterVo> list=new ArrayList<>();
        Optional.ofNullable(menus).orElse(new ArrayList<>())
                .stream()
                .filter(item-> item!=null&&item.getParentId().equals(pid))
                .forEach(item->{
                    RouterVo router = new RouterVo();
                    router.setName(item.getName());
                    router.setPath(item.getPath());
                    router.setChildren(makeRouter(menus,item.getMenuId()));

                    if(item.getParentId()==0L){
                        router.setComponent("Layout");
                        //判断该数据是否是菜单类型
                        if(item.getType().equals("1")){
                            router.setRedirect(item.getPath());
                            List<RouterVo> listChild=new ArrayList<>();
                            RouterVo child=new RouterVo();
                            child.setName(item.getName());
                            child.setPath(item.getPath());
                            child.setComponent(item.getUrl());
                            child.setMeta(child.new Meta(
                                    item.getTitle(),
                                    item.getIcon(),
                                    item.getCode().split(",")
                            ));
                            listChild.add(child);
                            router.setChildren(listChild);
                            router.setPath(item.getPath());
                            router.setName(item.getName());
                        }
                    }else{
                        router.setComponent(item.getUrl());
                    }

                    router.setMeta(router.new Meta(
                            item.getTitle(),
                            item.getIcon(),
                            item.getCode().split(",")
                    ));
                    list.add(router);
                });
        return list;
    }
}
