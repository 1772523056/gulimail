package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.BrandDao;
import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.vo.CateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public CateVo[] queryCateList(Long brandId) {
//        IPage<CategoryBrandRelationEntity> page = this.page(
//                new Query<CategoryBrandRelationEntity>().getPage(params),
//                new QueryWrapper<CategoryBrandRelationEntity>()
//        );
        List<CategoryBrandRelationEntity> categoryBrandRelationEntity = this.baseMapper.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        List<CateVo> blist=new ArrayList<>();
        for (CategoryBrandRelationEntity brandRelationEntity : categoryBrandRelationEntity) {
            CateVo cateVo = new CateVo();

            Long catelogId = brandRelationEntity.getCatelogId();
            String name = null;
            if (catelogId != null) {
                name = categoryDao.selectById(catelogId).getName();
            }
            cateVo.setCatelogId(catelogId);
            cateVo.setCatelogName(name);
            blist.add(cateVo);
        }


        return blist.toArray(new CateVo[0]);
    }

    @Override
    public BrandEntity[] queryBrandList(Long cateId) {
        List<CategoryBrandRelationEntity> catelog_id = this.baseMapper.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", cateId));
        List<Long> collect = catelog_id.stream().map(ele -> ele.getBrandId()).collect(Collectors.toList());
        List<BrandEntity> brandEntities = brandDao.selectBatchIds(collect);
        return brandEntities.toArray(new BrandEntity[brandEntities.size()]);
    }

    @Override
    public void saveBrandRelation(CategoryBrandRelationEntity categoryBrandRelation) {
        BrandEntity brandEntity = brandDao.selectById(categoryBrandRelation.getBrandId());
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryDao.selectById(categoryBrandRelation.getCatelogId()).getName());
        this.save(categoryBrandRelation);
    }

}