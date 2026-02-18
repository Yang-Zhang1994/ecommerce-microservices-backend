package com.atguigu.gulimall.coupon.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.to.SpuBoundTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.coupon.entity.SpuBoundsEntity;
import com.atguigu.gulimall.coupon.service.SpuBoundsService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品spu积分设置
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@RestController
@RequestMapping("coupon/spubounds")
public class SpuBoundsController {

    private static final Logger log = LoggerFactory.getLogger(SpuBoundsController.class);

    @Autowired
    private SpuBoundsService spuBoundsService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:spubounds:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuBoundsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:spubounds:info")
    public R info(@PathVariable("id") Long id){
		SpuBoundsEntity spuBounds = spuBoundsService.getById(id);

        return R.ok().put("spuBounds", spuBounds);
    }

    /**
     * 保存（远程调用传入 SpuBoundTo，无 id/work；转换为 Entity 后持久化）
     */
    @PostMapping("/save")
    //@RequiresPermissions("coupon:spubounds:save")
    public R save(@RequestBody SpuBoundTo to) {
        if (to == null) {
            log.warn("saveSpuBounds: body null");
            return R.error("请求体不能为空");
        }
        SpuBoundsEntity entity = new SpuBoundsEntity();
        entity.setSpuId(to.getSpuId());
        entity.setBuyBounds(to.getBuyBounds() != null ? to.getBuyBounds() : BigDecimal.ZERO);
        entity.setGrowBounds(to.getGrowBounds() != null ? to.getGrowBounds() : BigDecimal.ZERO);
        entity.setWork(0); // 默认：无优惠不赠送
        try {
            spuBoundsService.save(entity);
            return R.ok();
        } catch (Exception e) {
            log.error("saveSpuBounds failed for spuId={}", to.getSpuId(), e);
            throw e;
        }
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:spubounds:update")
    public R update(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.updateById(spuBounds);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:spubounds:delete")
    public R delete(@RequestBody Long[] ids){
		spuBoundsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
