package com.xq.web.equipment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.equipment.entity.Material;
import com.xq.web.equipment.mapper.MaterialMapper;
import org.springframework.stereotype.Service;

@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper,Material> implements MaterialService {
}
