package com.atguigu.common.to.es;
//"mappings": {

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


//        "attrs": {
//        "type": "nested",
//        "properties": {
//        "attrId": {
//        "type": "long"
//        },
//        "attrName": {
//        "type": "keyword",
//        "index": false,
//        "doc_values": false
//        },
//        "attrValue": {
//        "type": "keyword"
//        }
//        }
//        }
//        }
//        }
@Data
public class SkuEsModel {
    //        "properties": {
//        "skuId": {
//        "type": "long"
//        },
//        "spuId": {
//        "type": "keyword"
//        },
//        "skuTitle": {
//        "type": "text",
//        "analyzer": "ik_smart"
//        },
//        "skuPrice": {
//        "type": "keyword"
//        },
//        "skuImg": {
//        "type": "keyword",
//        "index": false,
//        "doc_values": false
//        },
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImag;
    //        "saleCount": {
//        "type": "long"
//        },
//        "hasStock": {
//        "type": "boolean"
//        },
//        "hotScore": {
//        "type": "long"
//        },
//        "brandId": {
//        "type": "long"
//        },
//        "catalogId": {
//        "type": "long"
//        },
//        "brandName": {
//        "type": "keyword",
//        "index": false,
//        "doc_values": false
//        },
//        "brandImg": {
//        "type": "keyword",
//        "index": false,
//        "doc_values": false
//        },
//        "catalogName": {
//        "type": "keyword",
//        "index": false,
//        "doc_values": false
//        },
    private Long saleCount;
    private boolean hasStock;
    private Long hotScore;
    private Long BrandId;
    private Long CatalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

}
