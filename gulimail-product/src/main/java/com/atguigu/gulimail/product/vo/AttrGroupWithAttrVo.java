package com.atguigu.gulimail.product.vo;

import com.atguigu.gulimail.product.entity.AttrEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class AttrGroupWithAttrVo {

    private Long attrGroupId;
    /**
     * ����
     */
    private String attrGroupName;
    /**
     * ����
     */
    private Integer sort;
    /**
     * ����
     */
    private String descript;
    /**
     * ��ͼ��
     */
    private String icon;
    /**
     * ��������id
     */
    private Long catelogId;

    private AttrEntity[] attrs;
}
