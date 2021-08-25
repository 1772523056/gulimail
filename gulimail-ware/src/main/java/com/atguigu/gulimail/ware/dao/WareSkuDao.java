package com.atguigu.gulimail.ware.dao;

import com.atguigu.gulimail.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-14 04:32:51
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    long getSkuStock(@Param("ele") Long ele);
}
