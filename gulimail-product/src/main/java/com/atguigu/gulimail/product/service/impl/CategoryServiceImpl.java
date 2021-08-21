package com.atguigu.gulimail.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        return categoryEntities.stream()
                .filter(ele -> ele.getParentCid() == 0)
                .map(ele -> {
                    ele.setChildren(getChildrens(ele, categoryEntities));
                    return ele;
                })
                .sorted(Comparator.comparingInt(num -> (num.getSort() == null ? 0 : num.getSort())))
                .collect(Collectors.toList());

    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 判断是否被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] getCategoryPath(Long catelogId) {
        ArrayList<Long> list = new ArrayList<>();
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid != 0) {
            getPath(parentCid, list);
        }
        list.add(catelogId);
        return  list.toArray(new Long[0]);

    }

    private void getPath(Long id, ArrayList<Long> list) {
        CategoryEntity categoryEntity = baseMapper.selectById(id);
        if (categoryEntity.getParentCid() != 0) {
            getPath(categoryEntity.getParentCid(), list);
        }
        list.add(id);
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(ele -> ele.getParentCid() == root.getCatId())
                .map(ele -> {
                    ele.setChildren(getChildrens(ele, all));
                    return ele;
                })
                .sorted(Comparator.comparingInt(num -> (num.getSort() == null ? 0 : num.getSort())))
                .collect(Collectors.toList());
    }
}