package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.AttrGroupWithAttrVo;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.atguigu.gulimail.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * ���Է���
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-08-16 09:17:43
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    AttrGroupWithAttrVo[] getAttrGroupWithAttr(long catelogId);

    List<SpuItemAttrGroupVo> getSpuItemAttrGroupVoWithAttrsBySpuId(Long spuId, Long catalogId);
}

