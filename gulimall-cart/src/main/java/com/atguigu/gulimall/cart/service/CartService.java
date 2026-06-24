package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.Cart;

/**
 * Cart domain service for add-to-cart flow.
 */
public interface CartService {

    /**
     * Adds SKU to cart and returns redirect URL for success page.
     */
    String addToCartAndBuildRedirect(Long skuId, Integer num, String title, String img, UserInfoTo userInfo);

    /**
     * Reads current user's cart from Redis (CartItem JSON in hash values).
     */
    Cart getCurrentCart(UserInfoTo userInfo);

    /**
     * Updates quantity of a single cart line by skuId.
     */
    void updateItemCount(Long skuId, Integer num, UserInfoTo userInfo);

    /**
     * Updates checked status of a single cart line by skuId.
     */
    void checkItem(Long skuId, Boolean checked, UserInfoTo userInfo);

    /**
     * Removes a single cart line by skuId.
     */
    void deleteItem(Long skuId, UserInfoTo userInfo);
}
