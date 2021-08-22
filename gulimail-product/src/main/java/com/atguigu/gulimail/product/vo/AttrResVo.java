package com.atguigu.gulimail.product.vo;

import lombok.Data;

@Data
public class AttrResVo extends AttrVo {
    private String catelogName;
    private String groupName;
    private String me;
    private String lk;

    private Long[] catelogPath;
}
