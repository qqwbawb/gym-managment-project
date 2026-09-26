package com.xq.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 封装返回值数据
 * @param <T>
 */
@Data
@AllArgsConstructor
public class ResultVo<T>{

    private String message;
    private int code;
    private T data;
}
