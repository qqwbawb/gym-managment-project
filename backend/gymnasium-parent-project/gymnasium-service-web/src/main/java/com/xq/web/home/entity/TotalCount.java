package com.xq.web.home.entity;

import lombok.Data;

@Data
public class TotalCount {

    private int memberCount;//会员总数

    private int userCount;//员工总数

    private int materCount;//器材总数

    private int orderCount;//订单总数
}
