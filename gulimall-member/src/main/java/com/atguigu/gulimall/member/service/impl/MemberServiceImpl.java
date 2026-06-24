package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.exception.BisException;
import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.to.member.MemberLoginTo;
import com.atguigu.common.to.member.MemberRegisterTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.entity.MemberOAuthBindEntity;
import com.atguigu.gulimall.member.repository.MemberLevelRepository;
import com.atguigu.gulimall.member.repository.MemberOAuthBindRepository;
import com.atguigu.gulimall.member.repository.MemberRepository;
import com.atguigu.gulimall.member.service.MemberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service("memberService")
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberOAuthBindRepository memberOAuthBindRepository;

    @Autowired
    private MemberLevelRepository memberLevelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberEntity>().getPageable(params, Sort.by("id").ascending());
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
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BisException(BisCodeEnum.VALID_EXCEPTION);
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BisException(BisCodeEnum.VALID_EXCEPTION);
        }
        MemberEntity member =
                memberRepository
                        .findById(id)
                        .orElseThrow(() -> new BisException(BisCodeEnum.UNKNOWN_EXCEPTION));
        member.setStatus(status);
        memberRepository.save(member);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        memberRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public Map<String, Object> register(MemberRegisterTo to) {
        if (to == null || StringUtils.isAnyBlank(to.getUsername(), to.getPassword())) {
            throw new BisException(BisCodeEnum.USERNAME_PASSWORD_REQUIRED);
        }
        String username = to.getUsername().trim();
        if (username.isEmpty()) {
            throw new BisException(BisCodeEnum.USERNAME_PASSWORD_REQUIRED);
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new BisException(BisCodeEnum.USERNAME_LENGTH_INVALID);
        }
        if (username.chars().allMatch(Character::isDigit)) {
            throw new BisException(BisCodeEnum.USERNAME_CANNOT_BE_ALL_NUMBERS);
        }
        String rawPassword = to.getPassword();
        if (rawPassword.length() < 6) {
            throw new BisException(BisCodeEnum.PASSWORD_TOO_SHORT);
        }
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new BisException(BisCodeEnum.USERNAME_ALREADY_EXISTS);
        }
        String mobile = StringUtils.trimToNull(to.getMobile());
        if (mobile == null) {
            throw new BisException(BisCodeEnum.MOBILE_NUMBER_REQUIRED);
        }
        if (memberRepository.findByMobile(mobile).isPresent()) {
            throw new BisException(BisCodeEnum.PHONE_ALREADY_REGISTERED);
        }
        MemberLevelEntity defaultLevel = memberLevelRepository.findFirstByDefaultStatus(1).orElse(null);
        if (defaultLevel == null) {
            throw new BisException(BisCodeEnum.DEFAULT_MEMBER_LEVEL_NOT_CONFIGURED);
        }
        MemberEntity entity = new MemberEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(rawPassword));
        entity.setMobile(mobile);
        entity.setLevelId(defaultLevel.getId());
        entity.setCreateTime(new Date());
        memberRepository.save(entity);
        return toMemberView(entity);
    }

    @Override
    public Map<String, Object> login(MemberLoginTo to) {
        if (to == null || StringUtils.isAnyBlank(to.getUsername(), to.getPassword())) {
            throw new BisException(BisCodeEnum.INVALID_USERNAME_OR_PASSWORD);
        }
        MemberEntity member = findMemberForLogin(to.getUsername().trim()).orElse(null);
        if (member == null || !passwordEncoder.matches(to.getPassword(), member.getPassword())) {
            throw new BisException(BisCodeEnum.INVALID_USERNAME_OR_PASSWORD);
        }
        if (member.getStatus() != null && member.getStatus() == 0) {
            throw new BisException(BisCodeEnum.ACCOUNT_DISABLED);
        }
        return toMemberView(member);
    }

    @Override
    @Transactional
    public Map<String, Object> oauthGoogle(MemberGoogleOAuthTo to) {
        if (to == null || StringUtils.isBlank(to.getSubject()) || StringUtils.isBlank(to.getProvider())) {
            throw new BisException(BisCodeEnum.OAUTH_IDENTIFIER_REQUIRED);
        }
        String provider = to.getProvider().trim();
        String subject = to.getSubject().trim();

        Optional<MemberOAuthBindEntity> existingBind =
                memberOAuthBindRepository.findByProviderAndProviderUserId(provider, subject);
        if (existingBind.isPresent()) {
            MemberEntity member =
                    memberRepository
                            .findById(existingBind.get().getMemberId())
                            .orElseThrow(() -> new BisException(BisCodeEnum.UNKNOWN_EXCEPTION));
            ensureActive(member);
            return toMemberView(member);
        }

        String email = StringUtils.trimToNull(to.getEmail());
        if (email != null) {
            Optional<MemberEntity> byEmail = memberRepository.findByEmail(email.toLowerCase());
            if (byEmail.isEmpty()) {
                byEmail = memberRepository.findByEmail(email);
            }
            if (byEmail.isPresent()) {
                MemberEntity member = byEmail.get();
                ensureActive(member);
                persistBind(provider, subject, member.getId());
                return toMemberView(member);
            }
        }

        MemberLevelEntity defaultLevel = memberLevelRepository.findFirstByDefaultStatus(1).orElse(null);
        if (defaultLevel == null) {
            throw new BisException(BisCodeEnum.DEFAULT_MEMBER_LEVEL_NOT_CONFIGURED);
        }

        MemberEntity entity = new MemberEntity();
        entity.setUsername(allocUsername(subject));
        entity.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        entity.setNickname(
                StringUtils.isNotBlank(to.getName()) ? to.getName().trim() : entity.getUsername());
        entity.setMobile(allocDistinctMobile(subject));
        entity.setEmail(email != null ? email.toLowerCase() : null);
        entity.setHeader(StringUtils.trimToNull(to.getPicture()));
        entity.setLevelId(defaultLevel.getId());
        entity.setCreateTime(new Date());
        entity.setStatus(1);
        memberRepository.save(entity);

        persistBind(provider, subject, entity.getId());
        return toMemberView(entity);
    }

    private static void ensureActive(MemberEntity member) {
        if (member.getStatus() != null && member.getStatus() == 0) {
            throw new BisException(BisCodeEnum.ACCOUNT_DISABLED);
        }
    }

    private void persistBind(String provider, String subject, Long memberId) {
        MemberOAuthBindEntity bind = new MemberOAuthBindEntity();
        bind.setMemberId(memberId);
        bind.setProvider(provider);
        bind.setProviderUserId(subject);
        bind.setCreatedAt(new Date());
        memberOAuthBindRepository.save(bind);
    }

    /**
     * Username rules match password registration (4–20 chars, not all digits); prefix keeps OAuth
     * accounts distinct.
     */
    private String allocUsername(String subject) {
        String hex = DigestUtils.md5DigestAsHex(subject.getBytes(StandardCharsets.UTF_8));
        for (int n = 0; n < 500; n++) {
            String suffix = n == 0 ? hex.substring(0, 16) : hex.substring(0, 12) + n;
            String candidate = ("g_" + suffix).substring(0, Math.min(20, ("g_" + suffix).length()));
            if (candidate.length() < 4) {
                continue;
            }
            if (candidate.chars().allMatch(Character::isDigit)) {
                continue;
            }
            if (memberRepository.findByUsername(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BisException(BisCodeEnum.UNKNOWN_EXCEPTION);
    }

    /** Synthetic 11-digit mobile unique in {@code ums_member} (OAuth users have no phone). */
    private String allocDistinctMobile(String subject) {
        for (int k = 0; k < 200; k++) {
            String hex =
                    DigestUtils.md5DigestAsHex((subject + "|m|" + k).getBytes(StandardCharsets.UTF_8));
            String mobile = "199" + hex.substring(0, 8);
            if (memberRepository.findByMobile(mobile).isEmpty()) {
                return mobile;
            }
        }
        throw new BisException(BisCodeEnum.UNKNOWN_EXCEPTION);
    }

    /** Login field may be username or mobile (same as stored after register). */
    private Optional<MemberEntity> findMemberForLogin(String account) {
        String id = StringUtils.trimToNull(account);
        if (id == null) {
            return Optional.empty();
        }
        return memberRepository.findByUsername(id).or(() -> memberRepository.findByMobile(id));
    }

    private static Map<String, Object> toMemberView(MemberEntity m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("username", m.getUsername());
        map.put("nickname", m.getNickname() != null ? m.getNickname() : m.getUsername());
        map.put("mobile", m.getMobile());
        map.put("email", m.getEmail());
        map.put("header", m.getHeader());
        map.put("gender", m.getGender());
        map.put("levelId", m.getLevelId());
        map.put("status", m.getStatus());
        return map;
    }
}
