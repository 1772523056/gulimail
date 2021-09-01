package com.atguigu.gulimail.product.vo;


import com.atguigu.gulimail.product.vo.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
//