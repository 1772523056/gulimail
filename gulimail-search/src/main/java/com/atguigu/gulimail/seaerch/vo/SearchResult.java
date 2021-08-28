package com.atguigu.gulimail.seaerch.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;
import java.util.List;

@Data
public class SearchResult {
    List<SkuEsModel> products;
    Long pageNumber;
    Long total;
    Long totalPages;

    List<BrandVo> brands;
    List<AttrVo> attrs;
    List<CatalogVo> catalogs;

    @Data
    public static class BrandVo {
        Long brandId;
        String brandName;
        String brandImg;
    }

    @Data
    public static class AttrVo {
        Long attrId;
        String attrName;
        List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        List catalogId;
        String catalogName;
    }
}
