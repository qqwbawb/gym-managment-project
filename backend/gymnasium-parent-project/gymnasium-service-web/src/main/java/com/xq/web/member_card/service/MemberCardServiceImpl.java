package com.xq.web.member_card.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xq.web.member_card.entity.MemberCard;
import com.xq.web.member_card.mapper.MemberCardMapper;
import org.springframework.stereotype.Service;

@Service
public class MemberCardServiceImpl extends ServiceImpl<MemberCardMapper, MemberCard> implements MemberCardService {

}
