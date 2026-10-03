package com.xq.web.sys_menu.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述路由信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {

    private String path;
    private String component;
    private String name;
    private String redirect;

    private Meta meta;

    @Data
    @AllArgsConstructor
    public class Meta{
        private String title;
        private String icon;
        private Object[] roles;
    }

    private List<RouterVo> children=new ArrayList<>();
}
