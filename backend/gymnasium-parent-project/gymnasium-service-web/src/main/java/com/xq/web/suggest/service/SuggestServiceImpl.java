package com.xq.web.suggest.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.suggest.entity.Suggest;
import com.xq.web.suggest.mapper.SuggestMapper;
import org.springframework.stereotype.Service;

@Service
public class SuggestServiceImpl extends ServiceImpl<SuggestMapper, Suggest> implements SuggestService {
}
