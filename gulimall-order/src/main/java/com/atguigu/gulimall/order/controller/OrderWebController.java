package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.interceptor.OrderLoginInterceptor;
import com.atguigu.gulimall.order.service.IdempotencyService;
import com.atguigu.gulimall.order.service.OrderMemberQueryService;
import com.atguigu.gulimall.order.service.OrderWorkflowService;
import com.atguigu.gulimall.order.vo.OrderDetailVo;
import com.atguigu.gulimall.order.vo.OrderListItemVo;
import com.atguigu.gulimall.order.to.OrderLoginUserTo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SeckillOrderBindVo;
import com.atguigu.gulimall.order.vo.SeckillOrderConfirmVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("order")
public class OrderWebController {
    private final OrderWorkflowService orderWorkflowService;
    private final OrderMemberQueryService orderMemberQueryService;
    private final IdempotencyService idempotencyService;

    public OrderWebController(
            OrderWorkflowService orderWorkflowService,
            OrderMemberQueryService orderMemberQueryService,
            IdempotencyService idempotencyService) {
        this.orderWorkflowService = orderWorkflowService;
        this.orderMemberQueryService = orderMemberQueryService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Order confirm payload for checkout page.
     */
    @GetMapping("/confirm")
    public R confirm(HttpServletRequest request) {
        OrderConfirmVo confirmVo = orderWorkflowService.confirmOrder(request.getHeader("Cookie"));
        return R.ok().put("data", confirmVo);
    }

    /**
     * Flash-sale checkout: pending order summary + member addresses before payment.
     */
    @GetMapping("/seckill/confirm")
    public R seckillConfirm(
            @RequestParam("orderSn") String orderSn,
            HttpServletRequest request) {
        try {
            SeckillOrderConfirmVo vo = orderWorkflowService.confirmSeckillOrder(
                    orderSn, request.getHeader("Cookie"));
            return R.ok().put("data", vo);
        } catch (IllegalStateException e) {
            String msg = e.getMessage() == null ? "Unable to load order" : e.getMessage();
            int code = "Please login first".equals(msg) ? 401 : 404;
            return R.error(code, msg);
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        }
    }

    /**
     * Bind shipping address to a pending flash-sale order, then client redirects to payment.
     */
    @PostMapping("/seckill/confirm")
    public R seckillBindAddress(
            @RequestBody SeckillOrderBindVo bindVo,
            HttpServletRequest request) {
        try {
            orderWorkflowService.bindSeckillOrderAddress(bindVo, request.getHeader("Cookie"));
            return R.ok();
        } catch (IllegalStateException e) {
            String msg = e.getMessage() == null ? "Unable to save address" : e.getMessage();
            int code = "Please login first".equals(msg) ? 401 : 400;
            return R.error(code, msg);
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        }
    }

    /**
     * Submit order from checkout page.
     */
    @PostMapping("/submit")
    public R submit(
            @RequestBody OrderSubmitVo submitVo,
            HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey) {
        OrderSubmitResponseVo resp = idempotencyService.execute(
                "order-submit",
                idempotencyKey,
                OrderSubmitResponseVo.class,
                () -> orderWorkflowService.submitOrder(submitVo, request.getHeader("Cookie")));
        if (resp.getCode() != null && resp.getCode() != 0) {
            return R.error(resp.getCode(), resp.getMsg());
        }
        return R.ok().put("data", resp);
    }

    /**
     * Cancel unpaid order (same member); notifies stock {@code order.release.other.*} + coupon queue.
     */
    @PostMapping("/cancel")
    public R cancel(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String orderSn = body == null ? null : body.get("orderSn");
        R r = orderWorkflowService.cancelOrder(orderSn, request.getHeader("Cookie"));
        if (r.getCode() != null && r.getCode() != 0) {
            return r;
        }
        return R.ok();
    }

    /**
     * Pay success callback (or local test). Publishes {@code order.finish.user} / {@code order.finish.ware} after commit.
     */
    @PostMapping("/pay/success")
    public R paySuccess(@RequestBody Map<String, String> body) {
        String orderSn = body == null ? null : body.get("orderSn");
        R r = orderWorkflowService.notifyPaySuccess(orderSn);
        if (r.getCode() != null && r.getCode() != 0) {
            return r;
        }
        return R.ok();
    }

    /**
     * Member order list (status tabs: all / pending / paid / closed).
     */
    @GetMapping("/list")
    public R list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        OrderLoginUserTo loginUser = OrderLoginInterceptor.THREAD_LOCAL.get();
        Long memberId = loginUser == null ? null : loginUser.getUserId();
        var result = orderMemberQueryService.listMemberOrders(
                memberId,
                status,
                page == null ? 1 : page,
                limit == null ? 10 : limit);
        return R.ok().put("data", result.getContent()).put("total", result.getTotalElements());
    }

    /**
     * Member order detail: header, line items, shipping address, payment summary.
     */
    @GetMapping("/detail/{orderSn}")
    public R detail(@PathVariable("orderSn") String orderSn) {
        OrderLoginUserTo loginUser = OrderLoginInterceptor.THREAD_LOCAL.get();
        Long memberId = loginUser == null ? null : loginUser.getUserId();
        OrderDetailVo detail = orderMemberQueryService.getMemberOrderDetail(memberId, orderSn);
        if (detail == null) {
            return R.error(404, "Order not found");
        }
        return R.ok().put("data", detail);
    }
}
