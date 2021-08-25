package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.AttrResVo;
import com.atguigu.gulimail.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * ��Ʒ����
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type);

    void saveAttr(AttrVo attr);

//    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    AttrResVo getAttrInfo(Long attrId);

    void updateAttr(AttrResVo attr);

    List<AttrEntity> getRelationAttr(Long id);

    PageUtils getNoRelation(Long id, Map<String, Object> params);

    List<Long> selectSearchAttrs(List<Long> id);
}

