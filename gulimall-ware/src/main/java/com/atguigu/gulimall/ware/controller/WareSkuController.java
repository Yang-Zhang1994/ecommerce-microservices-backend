package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.common.to.ware.SkuHasStockVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.vo.OrderLockStockResultVo;
import com.atguigu.gulimall.ware.vo.OrderWareLockVo;
import com.atguigu.gulimall.ware.vo.WareStockUnlockVo;



/**
 * 商品库存
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-02 13:48:24
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 批量查询 sku 是否有库存，供上架等场景使用
     */
    @RequestMapping("/hasStock")
    public R hasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVo> list = wareSkuService.hasStockBySkuIds(skuIds);
        return R.ok().put("data", list);
    }

    @RequestMapping("/locked-skus")
    public R lockedSkus(@RequestBody List<Long> skuIds) {
        return R.ok().put("data", wareSkuService.findSkuIdsWithLockedStock(skuIds));
    }

    @RequestMapping("/delete-by-sku-ids")
    public R deleteBySkuIds(@RequestBody List<Long> skuIds) {
        wareSkuService.deleteBySkuIds(skuIds);
        return R.ok();
    }

    /**
     * Lock stock for order create.
     */
    @RequestMapping("/lock")
    public R lock(@RequestBody OrderWareLockVo lockVo) {
        try {
            OrderLockStockResultVo result = wareSkuService.lockOrderStock(lockVo);
            return R.ok().put("data", result.getLocked()).put("taskId", result.getTaskId());
        } catch (Exception e) {
            return R.error(1, e.getMessage() == null ? "Lock stock failed" : e.getMessage());
        }
    }

    /**
     * Best-effort unlock previously locked stock.
     */
    @RequestMapping("/unlock")
    public R unlock(@RequestBody WareStockUnlockVo vo) {
        wareSkuService.unlockOrderStock(vo == null ? new WareStockUnlockVo() : vo);
        return R.ok();
    }

    /**
     * Pull latest SKU names from product service; fill null locked stock as 0.
     */
    @RequestMapping("/syncFromProduct")
    public R syncFromProduct() {
        Map<String, Object> stats = wareSkuService.syncFromProduct();
        return R.ok().put("data", stats);
    }

}
