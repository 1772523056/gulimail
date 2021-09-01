package com.atguigu.gulimail.seaerch.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword; //关键字
    private Long catalog3Id; //目录id
    private String sort; //按什么方式排序
    private Integer hasStock; //是否只显示有货
    private String skuPrice; //价格区间
    private List<Long>  brandId; //品牌id
    private List<String> attrs;//属性
    private Long pageNumber=1l;
    private String _queryString;//原生的查询条件

}
