package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.vo.CateVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;

/**
 * Ʒ�Ʒ������
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    CateVo[] queryCateList(Long brandId);

    BrandEntity[] queryBrandList(Long cateId);

    void saveBrandRelation(CategoryBrandRelationEntity categoryBrandRelation);
}

