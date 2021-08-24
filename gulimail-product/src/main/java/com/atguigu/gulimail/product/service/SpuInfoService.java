package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimail.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-18 08:49:34
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuInfo);

    PageUtils queryPageByCondition(Map<String, Object> params);

//    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);
}

