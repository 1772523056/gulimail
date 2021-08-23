package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.BrandVo;
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

    BrandVo[] queryPage(Long brandId);

    void saveBrandRelation(CategoryBrandRelationEntity categoryBrandRelation);
}

