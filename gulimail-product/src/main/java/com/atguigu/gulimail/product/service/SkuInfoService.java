package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku��Ϣ
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    SkuItemVo item(Long skuId);

}

