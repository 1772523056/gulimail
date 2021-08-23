package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.BrandDao;
import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimail.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimail.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;
    @Autowired
    CategoryDao categoryDao;

    @Override
    public BrandVo[] queryPage(Long brandId) {
//        IPage<CategoryBrandRelationEntity> page = this.page(
//                new Query<CategoryBrandRelationEntity>().getPage(params),
//                new QueryWrapper<CategoryBrandRelationEntity>()
//        );
        List<CategoryBrandRelationEntity> categoryBrandRelationEntity = this.baseMapper.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        List<BrandVo> blist=new ArrayList<>();
        for (CategoryBrandRelationEntity brandRelationEntity : categoryBrandRelationEntity) {
            BrandVo brandVo = new BrandVo();
            Long catelogId = brandRelationEntity.getCatelogId();
            String name = null;
            if (catelogId != null) {
                name = categoryDao.selectById(catelogId).getName();
            }
            brandVo.setCatelogId(catelogId);
            brandVo.setCatelogName(name);
            blist.add(brandVo);
        }


        return blist.toArray(new BrandVo[0]);
    }

    @Override
    public void saveBrandRelation(CategoryBrandRelationEntity categoryBrandRelation) {
        BrandEntity brandEntity = brandDao.selectById(categoryBrandRelation.getBrandId());
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryDao.selectById(categoryBrandRelation.getCatelogId()).getName());
        this.save(categoryBrandRelation);
    }

}