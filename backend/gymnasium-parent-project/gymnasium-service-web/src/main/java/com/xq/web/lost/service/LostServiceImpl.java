package com.xq.web.lost.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.lost.entity.Lost;
import com.xq.web.lost.mapper.LostMapper;
import org.springframework.stereotype.Service;

@Service
public class LostServiceImpl extends ServiceImpl<LostMapper, Lost> implements LostService {
}
