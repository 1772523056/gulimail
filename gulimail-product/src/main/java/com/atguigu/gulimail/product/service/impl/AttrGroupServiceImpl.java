package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimail.product.dao.AttrDao;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.AttrGroupDao;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
//            System.out.println(page);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }

    }

    @Override
    public AttrGroupWithAttrVo[] getAttrGroupWithAttr(long catelogId) {
        List<AttrGroupEntity> attrGroupEntityList = this.baseMapper.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));


        List<AttrGroupWithAttrVo> list = attrGroupEntityList.stream().map(ele -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(ele, attrGroupWithAttrVo);
            List<Long> attrIdList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().
                    eq("attr_group_id", ele.getAttrGroupId())).
                    stream().map(e -> e.getAttrId()).collect(Collectors.toList());
            List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIdList);
            attrGroupWithAttrVo.setAttrs(attrEntities.toArray(new AttrEntity[0]));
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());

        return list.toArray(new AttrGroupWithAttrVo[0]);


    }

}