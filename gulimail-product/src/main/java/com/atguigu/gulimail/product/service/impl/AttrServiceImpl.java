package com.atguigu.gulimail.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimail.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimail.product.dao.AttrDao;
import com.atguigu.gulimail.product.dao.AttrGroupDao;
import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.AttrService;
import com.atguigu.gulimail.product.vo.AttrResVo;
import com.atguigu.gulimail.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    CategoryServiceImpl categoryService;
//
//    @Override
//    public PageUtils queryPage(Map<String, Object> params, Long catelogId, String type) {
//        IPage<AttrEntity> page = this.page(
//                new Query<AttrEntity>().getPage(params),
//                new QueryWrapper<AttrEntity>()
//        );
//
//        return new PageUtils(page);
//    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.SALE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type) {
        String key = (String) params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().
                eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.SALE.getCode() : ProductConstant.AttrEnum.BASE.getCode());

        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);

        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getRecords();
        List<AttrResVo> attrResVoList = records.stream().map(ele -> {
            AttrResVo attrResVo = new AttrResVo();
            BeanUtils.copyProperties(ele, attrResVo);
            AttrAttrgroupRelationEntity attr_id = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", ele.getAttrId()));
            if ("base".equalsIgnoreCase(type)) {
                if (attr_id != null) {
                    AttrGroupEntity attr_group_id = attrGroupDao.selectById(attr_id.getAttrGroupId());
                    attrResVo.setGroupName(attr_group_id.getAttrGroupName());
                }
            }
            CategoryEntity cat_id = categoryDao.selectById(ele.getCatelogId());
            if (cat_id != null)
                attrResVo.setCatelogName(cat_id.getName());
            return attrResVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrResVoList);
        return pageUtils;

    }

    @Override
    public AttrResVo getAttrInfo(Long attrId) {
        AttrResVo attrResVo = new AttrResVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.SALE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
            attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }
        Long catelogId = attrEntity.getCatelogId();
        Long[] categoryPath = categoryService.getCategoryPath(catelogId);
        attrResVo.setCatelogPath(categoryPath);
        return attrResVo;

    }

    @Transactional
    @Override
    public void updateAttr(AttrResVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.SALE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attr.getAttrId()));
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long id) {
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", id));
        List<Long> collect = list.stream().map(ele -> ele.getAttrId()).collect(Collectors.toList());
        if (collect == null || collect.size() == 0)
            return null;
        return (List<AttrEntity>) this.listByIds(collect);
    }

}