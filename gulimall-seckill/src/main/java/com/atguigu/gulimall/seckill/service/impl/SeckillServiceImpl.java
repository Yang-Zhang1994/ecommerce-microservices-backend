package com.atguigu.gulimall.seckill.service.impl;

import com.atguigu.common.client.CouponApi;
import com.atguigu.common.client.ProductApi;
import com.atguigu.common.to.SeckillOrderCreateTo;
import com.atguigu.common.to.SeckillSessionVo;
import com.atguigu.common.to.SeckillSkuRelationVo;
import com.atguigu.common.to.SeckillWarmupSessionVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.constant.SeckillConstant;
import com.atguigu.gulimall.seckill.service.SeckillOrderPublisher;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillOrderGrabMeta;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.to.SkuInfoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);

    /**
     * Atomically reserve user quota: returns new total, or -1 when {@code current + add > limit}.
     * Sets TTL only when the key is first created.
     */
    private static final DefaultRedisScript<Long> RESERVE_USER_QUOTA_SCRIPT = new DefaultRedisScript<>(
            """
                    local key = KEYS[1]
                    local add = tonumber(ARGV[1])
                    local limit = tonumber(ARGV[2])
                    local ttl = tonumber(ARGV[3])
                    local cur = tonumber(redis.call('GET', key) or '0')
                    if cur + add > limit then
                      return -1
                    end
                    local next = redis.call('INCRBY', key, add)
                    if cur == 0 then
                      redis.call('EXPIRE', key, ttl)
                    end
                    return next
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final CouponApi couponApi;
    private final SeckillOrderPublisher seckillOrderPublisher;
    private final ProductApi productApi;
    private final ObjectMapper objectMapper;

    @Value("${seckill.warmup-days:3}")
    private int warmupDays;

    public SeckillServiceImpl(StringRedisTemplate redisTemplate,
                              RedissonClient redissonClient,
                              CouponApi couponApi,
                              SeckillOrderPublisher seckillOrderPublisher,
                              ProductApi productApi,
                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.couponApi = couponApi;
        this.seckillOrderPublisher = seckillOrderPublisher;
        this.productApi = productApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public void uploadSeckillSkuLatest3Days() {
        uploadSeckillSkuLatest3Days(false);
    }

    @Override
    public void uploadSeckillSkuLatest3Days(boolean refreshStock) {
        RLock lock = redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        boolean locked;
        try {
            locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!locked) {
            return;
        }
        try {
            List<SeckillWarmupSessionVo> bundles = fetchWarmupFromCoupon();
            Set<String> activeSessionKeys = new HashSet<>();
            if (bundles.isEmpty()) {
                log.debug("Seckill warm-up: coupon returned no enabled sessions with SKUs");
            } else {
                for (SeckillWarmupSessionVo bundle : bundles) {
                    String sessionKey = warmSession(bundle, refreshStock);
                    if (sessionKey != null) {
                        activeSessionKeys.add(sessionKey);
                    }
                }
                log.info("Seckill warm-up done: {} session(s) via coupon HTTP", bundles.size());
            }
            pruneStaleSessionKeys(activeSessionKeys);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private List<SeckillWarmupSessionVo> fetchWarmupFromCoupon() {
        try {
            R response = couponApi.seckillWarmup(Math.max(1, warmupDays));
            if (response == null || response.getCode() == null || response.getCode() != 0) {
                log.warn("Coupon seckillWarmup failed: {}", response);
                return List.of();
            }
            Object data = response.get("data");
            if (data == null) {
                return List.of();
            }
            return objectMapper.convertValue(data, new TypeReference<List<SeckillWarmupSessionVo>>() {});
        } catch (Exception e) {
            log.error("Failed to fetch seckill warm-up data from coupon", e);
            return List.of();
        }
    }

    private String warmSession(SeckillWarmupSessionVo bundle, boolean refreshStock) {
        SeckillSessionVo session = bundle.getSession();
        List<SeckillSkuRelationVo> relations = bundle.getSkus();
        if (session == null || session.getStartTime() == null || session.getEndTime() == null
                || relations == null || relations.isEmpty()) {
            return null;
        }
        long start = session.getStartTime().getTime();
        long end = session.getEndTime().getTime();
        String sessionKey = SeckillConstant.SESSION_CACHE_PREFIX + session.getId() + ":" + start + "_" + end;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        List<String> fieldIds = new ArrayList<>(relations.size());
        for (SeckillSkuRelationVo relation : relations) {
            String field = session.getId() + "_" + relation.getSkuId();
            fieldIds.add(field);
            upsertWarmSku(hashOps, field, relation, start, end, refreshStock);
        }

        redisTemplate.delete(sessionKey);
        if (!fieldIds.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(sessionKey, fieldIds.toArray(new String[0]));
        }
        long ttlSeconds = Math.max(60, (end - System.currentTimeMillis()) / 1000);
        redisTemplate.expire(sessionKey, Duration.ofSeconds(ttlSeconds));
        pruneOtherSessionKeys(session.getId(), sessionKey);
        return sessionKey;
    }

    /** Drop list keys for the same session when admin changes the time window. */
    private void pruneOtherSessionKeys(Long sessionId, String keepKey) {
        if (sessionId == null || !StringUtils.hasText(keepKey)) {
            return;
        }
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + sessionId + ":*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            if (!keepKey.equals(key)) {
                redisTemplate.delete(key);
            }
        }
    }

    /** Insert new SKU hash or refresh session window / price fields; keep randomCode when present. */
    private void upsertWarmSku(HashOperations<String, String, String> hashOps,
                               String field,
                               SeckillSkuRelationVo relation,
                               long start,
                               long end,
                               boolean refreshStock) {
        SeckillSkuRedisTo to = null;
        String existingJson = hashOps.get(SeckillConstant.SKUKILL_CACHE_PREFIX, field);
        if (StringUtils.hasText(existingJson)) {
            to = parse(existingJson);
        }
        java.math.BigDecimal previousCount = to != null ? to.getSeckillCount() : null;
        boolean isNew = to == null;
        if (isNew) {
            to = buildRedisTo(relation, start, end);
        } else {
            to.setSeckillPrice(relation.getSeckillPrice());
            to.setSeckillCount(relation.getSeckillCount());
            to.setSeckillLimit(relation.getSeckillLimit());
            to.setSeckillSort(relation.getSeckillSort());
            to.setStartTime(start);
            to.setEndTime(end);
            if (to.getSkuInfo() == null) {
                to.setSkuInfo(fetchSkuInfo(relation.getSkuId()));
            }
        }
        int targetStock = relation.getSeckillCount() == null ? 0 : relation.getSeckillCount().intValue();
        boolean countChanged = previousCount == null || previousCount.intValue() != targetStock;
        if (isNew) {
            initSemaphore(to.getRandomCode(), targetStock);
        } else if (refreshStock || countChanged) {
            resetSemaphore(to.getRandomCode(), targetStock);
        }
        try {
            hashOps.put(SeckillConstant.SKUKILL_CACHE_PREFIX, field,
                    objectMapper.writeValueAsString(to));
        } catch (JsonProcessingException e) {
            log.warn("Skip warming seckill field {}: serialize failed", field, e);
        }
    }

    private void initSemaphore(String randomCode, int permits) {
        if (!StringUtils.hasText(randomCode)) {
            return;
        }
        RSemaphore semaphore = redissonClient
                .getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
        semaphore.trySetPermits(Math.max(0, permits));
    }

    /** Replace drained/incorrect semaphore state with the configured seckill stock. */
    private void resetSemaphore(String randomCode, int permits) {
        if (!StringUtils.hasText(randomCode)) {
            return;
        }
        RSemaphore semaphore = redissonClient
                .getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
        semaphore.delete();
        semaphore.trySetPermits(Math.max(0, permits));
    }

    private void pruneStaleSessionKeys(Set<String> activeSessionKeys) {
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            if (!activeSessionKeys.contains(key)) {
                redisTemplate.delete(key);
            }
        }
    }

    private SeckillSkuRedisTo buildRedisTo(SeckillSkuRelationVo relation, long start, long end) {
        SeckillSkuRedisTo to = new SeckillSkuRedisTo();
        to.setId(relation.getId());
        to.setPromotionId(relation.getPromotionId());
        to.setPromotionSessionId(relation.getPromotionSessionId());
        to.setSkuId(relation.getSkuId());
        to.setSeckillPrice(relation.getSeckillPrice());
        to.setSeckillCount(relation.getSeckillCount());
        to.setSeckillLimit(relation.getSeckillLimit());
        to.setSeckillSort(relation.getSeckillSort());
        to.setStartTime(start);
        to.setEndTime(end);
        to.setRandomCode(UUID.randomUUID().toString().replace("-", ""));
        to.setSkuInfo(fetchSkuInfo(relation.getSkuId()));
        return to;
    }

    private SkuInfoVo fetchSkuInfo(Long skuId) {
        try {
            R r = productApi.info(skuId);
            Object skuInfo = r == null ? null : r.get("skuInfo");
            if (skuInfo == null) {
                return null;
            }
            return objectMapper.convertValue(skuInfo, SkuInfoVo.class);
        } catch (Exception e) {
            log.warn("Could not fetch product info for skuId={} during warm-up", skuId, e);
            return null;
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long now = System.currentTimeMillis();
        return listSessionSkus(now, (start, end) -> now >= start && now <= end, false);
    }

    @Override
    public List<SeckillSkuRedisTo> getUpcomingSeckillSkus() {
        long now = System.currentTimeMillis();
        long horizon = now + Duration.ofDays(Math.max(1, warmupDays)).toMillis();
        return listSessionSkus(now, (start, end) -> now < start && start <= horizon, true);
    }

    private List<SeckillSkuRedisTo> listSessionSkus(long now,
                                                    BiPredicate<Long, Long> windowMatch,
                                                    boolean stripRandomCode) {
        Set<String> keys = redisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, SeckillSkuRedisTo> deduped = new LinkedHashMap<>();
        for (String key : keys) {
            List<String> fields = redisTemplate.opsForList().range(key, 0, -1);
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            List<String> jsonList = hashOps.multiGet(SeckillConstant.SKUKILL_CACHE_PREFIX, fields);
            for (String json : jsonList) {
                if (!StringUtils.hasText(json)) {
                    continue;
                }
                SeckillSkuRedisTo to = parse(json);
                if (to == null || to.getStartTime() == null || to.getEndTime() == null) {
                    continue;
                }
                long start = to.getStartTime();
                long end = to.getEndTime();
                if (!windowMatch.test(start, end)) {
                    continue;
                }
                boolean live = now >= start && now <= end;
                if (stripRandomCode || !live) {
                    to.setRandomCode(null);
                }
                String dedupeKey = to.getPromotionSessionId() + "_" + to.getSkuId();
                deduped.merge(dedupeKey, to, (left, right) -> {
                    boolean leftLive = now >= left.getStartTime() && now <= left.getEndTime();
                    boolean rightLive = now >= right.getStartTime() && now <= right.getEndTime();
                    if (leftLive != rightLive) {
                        return leftLive ? left : right;
                    }
                    return left.getStartTime() <= right.getStartTime() ? left : right;
                });
            }
        }
        List<SeckillSkuRedisTo> result = new ArrayList<>(deduped.values());
        result.sort(Comparator
                .comparing(SeckillSkuRedisTo::getStartTime, Comparator.nullsLast(Long::compareTo))
                .thenComparing(SeckillSkuRedisTo::getSeckillSort, Comparator.nullsLast(Integer::compareTo)));
        return result;
    }

    /** Supports {@code seckill:sessions:{sessionId}:{start}_{end}} and legacy {@code seckill:sessions:{start}_{end}}. */
    private static long[] parseSessionWindow(String sessionKey) {
        if (!StringUtils.hasText(sessionKey)) {
            return null;
        }
        String suffix = sessionKey.substring(SeckillConstant.SESSION_CACHE_PREFIX.length());
        String windowPart = suffix;
        int colon = suffix.indexOf(':');
        if (colon >= 0) {
            windowPart = suffix.substring(colon + 1);
        }
        String[] parts = windowPart.split("_");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new long[] { Long.parseLong(parts[0]), Long.parseLong(parts[1]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {
        if (skuId == null) {
            return null;
        }
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Set<String> fields = hashOps.keys(SeckillConstant.SKUKILL_CACHE_PREFIX);
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        String suffix = "_" + skuId;
        long now = System.currentTimeMillis();
        long horizon = now + Duration.ofDays(Math.max(1, warmupDays)).toMillis();
        SeckillSkuRedisTo nearestUpcoming = null;
        for (String field : fields) {
            if (!field.endsWith(suffix)) {
                continue;
            }
            String json = hashOps.get(SeckillConstant.SKUKILL_CACHE_PREFIX, field);
            SeckillSkuRedisTo to = parse(json);
            if (to == null || to.getStartTime() == null || to.getEndTime() == null) {
                continue;
            }
            if (now >= to.getStartTime() && now <= to.getEndTime()) {
                return to;
            }
            if (now < to.getStartTime() && to.getStartTime() <= horizon) {
                if (nearestUpcoming == null || to.getStartTime() < nearestUpcoming.getStartTime()) {
                    to.setRandomCode(null);
                    nearestUpcoming = to;
                }
            }
        }
        return nearestUpcoming;
    }

    @Override
    public String kill(String killId, String key, int num, Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException("Please login first");
        }
        if (!StringUtils.hasText(killId)) {
            throw new IllegalArgumentException("Invalid seckill id");
        }
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String json = hashOps.get(SeckillConstant.SKUKILL_CACHE_PREFIX, killId);
        SeckillSkuRedisTo to = parse(json);
        if (to == null) {
            throw new IllegalArgumentException("Seckill item not available");
        }

        long now = System.currentTimeMillis();
        if (to.getStartTime() == null || to.getEndTime() == null
                || now < to.getStartTime() || now > to.getEndTime()) {
            throw new IllegalStateException("Seckill is not active");
        }
        String expectedId = to.getPromotionSessionId() + "_" + to.getSkuId();
        if (!expectedId.equals(killId) || to.getRandomCode() == null || !to.getRandomCode().equals(key)) {
            throw new IllegalArgumentException("Invalid seckill request");
        }
        int limit = to.getSeckillLimit() == null ? 1 : to.getSeckillLimit().intValue();
        if (num <= 0 || num > limit) {
            throw new IllegalArgumentException("Quantity exceeds purchase limit");
        }

        String userFlag = SeckillConstant.USER_BUY_FLAG + memberId + "_" + killId;
        long ttlSeconds = Math.max(1, (to.getEndTime() - now) / 1000);

        RSemaphore semaphore = redissonClient
                .getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + to.getRandomCode());
        boolean acquired;
        try {
            acquired = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        if (!acquired) {
            return null;
        }

        Long reserved = reserveUserQuota(userFlag, num, limit, ttlSeconds);
        if (reserved == null || reserved < 0) {
            semaphore.release(num);
            int remaining = Math.max(0, limit - currentUserGrabCount(userFlag));
            if (remaining <= 0) {
                throw new IllegalStateException("You have reached the purchase limit for this item");
            }
            throw new IllegalStateException(
                    "Purchase limit is " + limit + "; you can grab at most " + remaining + " more now");
        }

        String orderSn = generateOrderSn();
        SeckillOrderCreateTo order = new SeckillOrderCreateTo();
        order.setOrderSn(orderSn);
        order.setMemberId(memberId);
        order.setPromotionSessionId(to.getPromotionSessionId());
        order.setSkuId(to.getSkuId());
        order.setSeckillPrice(to.getSeckillPrice());
        order.setNum(num);
        try {
            seckillOrderPublisher.publishOrderCreate(order);
            saveGrabMeta(orderSn, memberId, killId, to.getRandomCode(), num, ttlSeconds);
            return orderSn;
        } catch (RuntimeException e) {
            releaseUserQuota(userFlag, num);
            semaphore.release(num);
            throw e;
        }
    }

    @Override
    public void rollbackUnpaidGrab(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return;
        }
        String metaKey = SeckillConstant.ORDER_GRAB_META_PREFIX + orderSn.trim();
        String json = redisTemplate.opsForValue().getAndDelete(metaKey);
        if (!StringUtils.hasText(json)) {
            return;
        }
        SeckillOrderGrabMeta meta;
        try {
            meta = objectMapper.readValue(json, SeckillOrderGrabMeta.class);
        } catch (JsonProcessingException e) {
            log.warn("Invalid seckill grab meta orderSn={}", orderSn, e);
            return;
        }
        if (meta.getMemberId() == null || !StringUtils.hasText(meta.getKillId()) || meta.getNum() <= 0) {
            return;
        }
        String userFlag = SeckillConstant.USER_BUY_FLAG + meta.getMemberId() + "_" + meta.getKillId();
        releaseUserQuota(userFlag, meta.getNum());
        if (StringUtils.hasText(meta.getRandomCode())) {
            RSemaphore semaphore = redissonClient
                    .getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + meta.getRandomCode());
            semaphore.release(meta.getNum());
        }
        log.info("Seckill grab rolled back orderSn={} memberId={} killId={} num={}",
                orderSn, meta.getMemberId(), meta.getKillId(), meta.getNum());
    }

    private Long reserveUserQuota(String userFlag, int num, int limit, long ttlSeconds) {
        return redisTemplate.execute(
                RESERVE_USER_QUOTA_SCRIPT,
                Collections.singletonList(userFlag),
                String.valueOf(num),
                String.valueOf(limit),
                String.valueOf(ttlSeconds)
        );
    }

    private int currentUserGrabCount(String userFlag) {
        String raw = redisTemplate.opsForValue().get(userFlag);
        if (!StringUtils.hasText(raw)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void releaseUserQuota(String userFlag, int num) {
        if (num <= 0) {
            return;
        }
        Long left = redisTemplate.opsForValue().increment(userFlag, -num);
        if (left != null && left <= 0) {
            redisTemplate.delete(userFlag);
        }
    }

    private void saveGrabMeta(
            String orderSn,
            Long memberId,
            String killId,
            String randomCode,
            int num,
            long ttlSeconds) {
        SeckillOrderGrabMeta meta = new SeckillOrderGrabMeta();
        meta.setOrderSn(orderSn);
        meta.setMemberId(memberId);
        meta.setKillId(killId);
        meta.setRandomCode(randomCode);
        meta.setNum(num);
        try {
            String json = objectMapper.writeValueAsString(meta);
            redisTemplate.opsForValue().set(
                    SeckillConstant.ORDER_GRAB_META_PREFIX + orderSn,
                    json,
                    Duration.ofSeconds(Math.max(60, ttlSeconds))
            );
        } catch (JsonProcessingException e) {
            log.warn("Could not persist seckill grab meta orderSn={}", orderSn, e);
        }
    }

    private SeckillSkuRedisTo parse(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SeckillSkuRedisTo.class);
        } catch (JsonProcessingException e) {
            log.warn("Could not parse cached seckill sku json", e);
            return null;
        }
    }

    private String generateOrderSn() {
        return System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
