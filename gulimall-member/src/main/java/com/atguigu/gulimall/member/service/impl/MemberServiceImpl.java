package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.repository.MemberRepository;
import com.atguigu.gulimall.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("memberService")
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberEntity>().getPageable(params);
        Page<MemberEntity> page = memberRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public MemberEntity getById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberEntity entity) {
        memberRepository.save(entity);
    }

    @Override
    public void updateById(MemberEntity entity) {
        memberRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        memberRepository.deleteAllById((Iterable<Long>) ids);
    }
}
