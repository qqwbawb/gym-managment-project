package com.xq.web.home.entity;

import lombok.Data;

@Data
public class TotalCount {

    private Long memberCount;//会员总数

    private Long userCount;//员工总数

    private Long materCount;//器材总数

    private Long orderCount;//订单总数
}
