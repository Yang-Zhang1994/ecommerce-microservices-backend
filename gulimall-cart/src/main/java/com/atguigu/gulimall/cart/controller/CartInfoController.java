package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.Cart;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartInfoController {

    private final StringRedisTemplate stringRedisTemplate;
    private final CartService cartService;

    @Value("${spring.application.name:gulimall-cart}")
    private String appName;

    public CartInfoController(StringRedisTemplate stringRedisTemplate, CartService cartService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cartService = cartService;
    }

    /**
     * Basic probe endpoint to verify the cart service is reachable via gateway/consul.
     */
    @GetMapping("/ping")
    public R ping() {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", appName);
        payload.put("ts", Instant.now().toString());
        payload.put("redisReady", stringRedisTemplate.getConnectionFactory() != null);
        payload.put("userId", userInfo == null ? null : userInfo.getUserId());
        payload.put("userKey", userInfo == null ? null : userInfo.getUserKey());
        payload.put("tempUser", userInfo != null && userInfo.isTempUser());
        return R.ok().put("data", payload);
    }

    /**
     * Adds SKU into current user's cart and redirects to static success page.
     * Gateway path: /api/cart/add?skuId=...&num=...
     */
    @GetMapping("/add")
    public void addToCart(@RequestParam("skuId") Long skuId,
                          @RequestParam(value = "num", defaultValue = "1") Integer num,
                          @RequestParam(value = "title", required = false) String title,
                          @RequestParam(value = "img", required = false) String img,
                          HttpServletResponse response) throws IOException {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        String redirectUrl = cartService.addToCartAndBuildRedirect(skuId, num, title, img, userInfo);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Returns cart items parsed from Redis hash JSON values.
     * Gateway path: /api/cart/current
     */
    @GetMapping("/current")
    public R currentCart() {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        Cart cart = cartService.getCurrentCart(userInfo);
        return R.ok().put("data", cart);
    }

    /**
     * Updates quantity for one cart item.
     * Gateway path: /api/cart/item/count
     */
    @PostMapping("/item/count")
    public R updateCount(@RequestParam("skuId") Long skuId,
                         @RequestParam("num") Integer num) {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        cartService.updateItemCount(skuId, num, userInfo);
        return R.ok();
    }

    /**
     * Updates checked state for one cart item.
     * Gateway path: /api/cart/item/check
     */
    @PostMapping("/item/check")
    public R updateCheck(@RequestParam("skuId") Long skuId,
                         @RequestParam("checked") Boolean checked) {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        cartService.checkItem(skuId, checked, userInfo);
        return R.ok();
    }

    /**
     * Deletes one cart item.
     * Gateway path: /api/cart/item/delete
     */
    @PostMapping("/item/delete")
    public R deleteItem(@RequestParam("skuId") Long skuId) {
        UserInfoTo userInfo = CartInterceptor.THREAD_LOCAL.get();
        cartService.deleteItem(skuId, userInfo);
        return R.ok();
    }
}
