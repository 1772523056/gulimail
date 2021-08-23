package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.AttrRelationVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * ����&���Է������
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void deleteRelation(AttrRelationVo[] relation);

    void saveBatch(List<AttrRelationVo> attrRelationVo);
}

