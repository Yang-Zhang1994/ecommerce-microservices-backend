package com.atguigu.gulimall.cart.service.impl;

import com.atguigu.common.client.ProductApi;
import com.atguigu.gulimall.cart.constant.CartConstant;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductApi productApi;
    private final ObjectMapper objectMapper;

    public CartServiceImpl(StringRedisTemplate stringRedisTemplate, ProductApi productApi, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.productApi = productApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public String addToCartAndBuildRedirect(Long skuId, Integer num, String title, String img, UserInfoTo userInfo) {
        if (skuId == null || skuId <= 0) {
            return "/cart/success?err=invalidSku";
        }
        int quantity = (num == null || num < 1) ? 1 : num;
        mergeTempCartIntoUserCartIfNeeded(userInfo);
        String cartKey = resolveCartKey(userInfo);
        String skuField = String.valueOf(skuId);
        Object oldObj = stringRedisTemplate.opsForHash().get(cartKey, skuField);
        CartItem cartItem = (oldObj == null)
                ? createCartItemFromRemote(skuId, quantity)
                : increaseExistingCartItem(String.valueOf(oldObj), quantity, skuId);
        String json = toJson(cartItem);
        stringRedisTemplate.opsForHash().put(cartKey, skuField, json);

        String resolvedTitle = (title != null && !title.isBlank()) ? title : cartItem.getTitle();
        String resolvedImg = (img != null && !img.isBlank()) ? img : cartItem.getImage();

        StringBuilder target = new StringBuilder("/cart/success?skuId=")
                .append(skuId)
                .append("&num=")
                .append(quantity);
        if (resolvedTitle != null && !resolvedTitle.isBlank()) {
            target.append("&title=").append(URLEncoder.encode(resolvedTitle, StandardCharsets.UTF_8));
        }
        if (resolvedImg != null && !resolvedImg.isBlank()) {
            target.append("&img=").append(URLEncoder.encode(resolvedImg, StandardCharsets.UTF_8));
        }
        return target.toString();
    }

    @Override
    public Cart getCurrentCart(UserInfoTo userInfo) {
        mergeTempCartIntoUserCartIfNeeded(userInfo);
        String cartKey = resolveCartKey(userInfo);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
        Cart cart = new Cart();
        if (entries == null || entries.isEmpty()) {
            return cart;
        }
        List<CartItem> items = entries.values().stream()
                .map(String::valueOf)
                .map(this::fromJson)
                .filter(Objects::nonNull)
                .toList();
        cart.setItems(items);
        return cart;
    }

    @Override
    public void updateItemCount(Long skuId, Integer num, UserInfoTo userInfo) {
        if (skuId == null || skuId <= 0) {
            return;
        }
        int target = (num == null || num < 1) ? 1 : num;
        mergeTempCartIntoUserCartIfNeeded(userInfo);
        String cartKey = resolveCartKey(userInfo);
        String skuField = String.valueOf(skuId);
        Object oldObj = stringRedisTemplate.opsForHash().get(cartKey, skuField);
        if (oldObj == null) {
            return;
        }
        CartItem item = fromJson(String.valueOf(oldObj));
        if (item == null) {
            return;
        }
        item.setCount(target);
        if (item.getSkuId() == null) {
            item.setSkuId(skuId);
        }
        stringRedisTemplate.opsForHash().put(cartKey, skuField, toJson(item));
    }

    @Override
    public void checkItem(Long skuId, Boolean checked, UserInfoTo userInfo) {
        if (skuId == null || skuId <= 0) {
            return;
        }
        mergeTempCartIntoUserCartIfNeeded(userInfo);
        String cartKey = resolveCartKey(userInfo);
        String skuField = String.valueOf(skuId);
        Object oldObj = stringRedisTemplate.opsForHash().get(cartKey, skuField);
        if (oldObj == null) {
            return;
        }
        CartItem item = fromJson(String.valueOf(oldObj));
        if (item == null) {
            return;
        }
        item.setCheck(Boolean.TRUE.equals(checked));
        if (item.getSkuId() == null) {
            item.setSkuId(skuId);
        }
        stringRedisTemplate.opsForHash().put(cartKey, skuField, toJson(item));
    }

    @Override
    public void deleteItem(Long skuId, UserInfoTo userInfo) {
        if (skuId == null || skuId <= 0) {
            return;
        }
        mergeTempCartIntoUserCartIfNeeded(userInfo);
        String cartKey = resolveCartKey(userInfo);
        stringRedisTemplate.opsForHash().delete(cartKey, String.valueOf(skuId));
    }

    private CartItem increaseExistingCartItem(String oldJson, int quantity, Long skuId) {
        try {
            CartItem existing = objectMapper.readValue(oldJson, CartItem.class);
            int oldCount = existing.getCount() == null ? 0 : existing.getCount();
            existing.setCount(Math.max(1, oldCount + quantity));
            if (existing.getSkuId() == null) {
                existing.setSkuId(skuId);
            }
            return existing;
        } catch (Exception ignored) {
            return createCartItemFromRemote(skuId, quantity);
        }
    }

    private CartItem createCartItemFromRemote(Long skuId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setSkuId(skuId);
        cartItem.setCheck(true);
        cartItem.setCount(quantity);

        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            try {
                var r = productApi.info(skuId);
                if (r == null) {
                    return;
                }
                Object raw = r.get("skuInfo");
                if (!(raw instanceof Map<?, ?> map)) {
                    return;
                }
                Object titleObj = map.get("skuTitle");
                Object imgObj = map.get("skuDefaultImg");
                Object priceObj = map.get("price");
                if (titleObj != null) {
                    cartItem.setTitle(String.valueOf(titleObj));
                }
                if (imgObj != null) {
                    cartItem.setImage(String.valueOf(imgObj));
                }
                if (priceObj != null) {
                    try {
                        cartItem.setPrice(new BigDecimal(String.valueOf(priceObj)));
                    } catch (NumberFormatException ignored) {
                        cartItem.setPrice(BigDecimal.ZERO);
                    }
                }
            } catch (Exception ignored) {
                // keep defaults
            }
        });

        CompletableFuture<Void> getSkuSaleAttrsTask = CompletableFuture.runAsync(() -> {
            try {
                var r = productApi.saleAttrStrings(skuId);
                if (r == null) {
                    return;
                }
                Object raw = r.get("data");
                if (raw == null) {
                    return;
                }
                List<String> values = objectMapper.convertValue(raw, new TypeReference<List<String>>() {});
                cartItem.setSkuAttr(values);
            } catch (Exception ignored) {
                // keep defaults
            }
        });

        CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrsTask).join();
        if (cartItem.getPrice() == null) {
            cartItem.setPrice(BigDecimal.ZERO);
        }
        if (cartItem.getTitle() == null || cartItem.getTitle().isBlank()) {
            cartItem.setTitle("SKU " + skuId);
        }
        return cartItem;
    }

    private String toJson(CartItem cartItem) {
        try {
            return objectMapper.writeValueAsString(cartItem);
        } catch (Exception ignored) {
            return "{\"skuId\":" + cartItem.getSkuId() + ",\"count\":" + cartItem.getCount() + "}";
        }
    }

    private CartItem fromJson(String json) {
        try {
            return objectMapper.readValue(json, CartItem.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 已登录且请求携带访客 user-key 时：把 {@code gulimall:cart:temp:{userKey}} 合并进
     * {@code gulimall:cart:user:{userId}}，同 SKU 数量相加；合并成功后删除临时 Redis key。
     */
    private void mergeTempCartIntoUserCartIfNeeded(UserInfoTo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return;
        }
        if (!StringUtils.hasText(userInfo.getUserKey())) {
            return;
        }
        String tempKey = CartConstant.CART_REDIS_PREFIX + "temp:" + userInfo.getUserKey().trim();
        String userCartKey = CartConstant.CART_REDIS_PREFIX + "user:" + userInfo.getUserId();

        Map<Object, Object> tempEntries = stringRedisTemplate.opsForHash().entries(tempKey);
        if (tempEntries == null || tempEntries.isEmpty()) {
            return;
        }

        var hashOps = stringRedisTemplate.opsForHash();
        for (Map.Entry<Object, Object> e : tempEntries.entrySet()) {
            String skuField = String.valueOf(e.getKey());
            String tempJson = e.getValue() == null ? "" : String.valueOf(e.getValue());
            if (!StringUtils.hasText(tempJson)) {
                continue;
            }
            Long skuId = parseSkuId(skuField, tempJson);
            if (skuId == null || skuId <= 0) {
                continue;
            }

            Object existingObj = hashOps.get(userCartKey, skuField);
            String mergedJson;
            if (existingObj == null) {
                CartItem fromTemp = fromJson(tempJson);
                if (fromTemp == null) {
                    continue;
                }
                if (fromTemp.getSkuId() == null) {
                    fromTemp.setSkuId(skuId);
                }
                mergedJson = toJson(fromTemp);
            } else {
                mergedJson = mergeTwoCartItemJson(String.valueOf(existingObj), tempJson, skuId);
            }
            hashOps.put(userCartKey, skuField, mergedJson);
        }

        stringRedisTemplate.delete(tempKey);
    }

    private Long parseSkuId(String hashField, String itemJson) {
        try {
            return Long.parseLong(hashField.trim());
        } catch (NumberFormatException ignored) {
            CartItem parsed = fromJson(itemJson);
            return parsed != null ? parsed.getSkuId() : null;
        }
    }

    /** 用户车与访客车同一 SKU：数量相加，缺省标题/图片从访客行补齐。 */
    private String mergeTwoCartItemJson(String userJson, String tempJson, Long skuId) {
        CartItem u = fromJson(userJson);
        CartItem t = fromJson(tempJson);
        if (u == null && t == null) {
            return "{\"skuId\":" + skuId + ",\"count\":1}";
        }
        if (u == null) {
            if (t.getSkuId() == null) {
                t.setSkuId(skuId);
            }
            return toJson(t);
        }
        if (t == null) {
            return toJson(u);
        }
        int uc = u.getCount() == null ? 0 : u.getCount();
        int tc = t.getCount() == null ? 0 : t.getCount();
        u.setCount(Math.max(1, uc + tc));
        if (u.getSkuId() == null) {
            u.setSkuId(skuId);
        }
        if ((u.getTitle() == null || u.getTitle().isBlank()) && StringUtils.hasText(t.getTitle())) {
            u.setTitle(t.getTitle());
        }
        if ((u.getImage() == null || u.getImage().isBlank()) && StringUtils.hasText(t.getImage())) {
            u.setImage(t.getImage());
        }
        return toJson(u);
    }

    private String resolveCartKey(UserInfoTo userInfo) {
        if (userInfo != null && userInfo.getUserId() != null) {
            return CartConstant.CART_REDIS_PREFIX + "user:" + userInfo.getUserId();
        }
        String userKey = (userInfo == null || userInfo.getUserKey() == null || userInfo.getUserKey().isBlank())
                ? "anonymous"
                : userInfo.getUserKey();
        return CartConstant.CART_REDIS_PREFIX + "temp:" + userKey;
    }
}
