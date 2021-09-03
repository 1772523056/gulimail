package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.SkuInfoEntity;
import com.atguigu.gulimail.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku��Ϣ
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

    List<SkuItemSaleAttrVo> selectSkuAttrsBySpuId(@Param("spuId") Long spuId);
}
