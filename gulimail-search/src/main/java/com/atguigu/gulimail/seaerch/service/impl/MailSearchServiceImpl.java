package com.atguigu.gulimail.seaerch.service.impl;

import com.atguigu.common.utils.Query;
import com.atguigu.gulimail.seaerch.config.ElasticSearchConfig;
import com.atguigu.gulimail.seaerch.constant.EsConstant;
import com.atguigu.gulimail.seaerch.service.MailSearchService;
import com.atguigu.gulimail.seaerch.vo.SearchParam;
import com.atguigu.gulimail.seaerch.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailSearchServiceImpl implements MailSearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult listPage(SearchParam searchParam) {
        //todo 动态构建出需要的dsl语句
        SearchResult searchResult = null;


        //准备检索请求
        SearchRequest searchRequest = buidSeaerchRequest(searchParam);

        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //todo 解析返回的json 封装成vo
            searchResult = buildSearchResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    private SearchRequest buidSeaerchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        //todo 1. 查询 query
        //todo 1.1 关键字模糊匹配 must
        if (!StringUtils.isEmpty(param.getKeyword())) {
            query.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //todo 1.2 根据选择过滤 filter
        //过滤三级分类id
        if (param.getCatalog3Id() != null) {
            query.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //过滤品牌id
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            query.filter(QueryBuilders.termsQuery("brandId", param.getBrandId().toArray(new Long[0])));
        }
        //按照是否有库存进行查询
        query.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        //按照价格范围进行查询
        String skuPrice = param.getSkuPrice();
        if (!StringUtils.isEmpty(skuPrice)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = skuPrice.split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (skuPrice.startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (skuPrice.endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            query.filter(rangeQuery);
        }

        //根据属性查询
        if (!StringUtils.isEmpty(param.getAttrs())) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一对都得生成一个nestedquery
                query.filter(QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None));
            }
        }
        //todo 2.排序 sort
        //根据升序或降序排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }

        sourceBuilder.from((int) ((param.getPageNumber() - 1) * EsConstant.PRODUCT_PAGESIZE));
        sourceBuilder.size(Math.toIntExact(EsConstant.PRODUCT_PAGESIZE));
        //todo 3.高亮 highlight
        //高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }


        //todo 4.聚合 aggregations
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(10);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(10));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_value_agg").field("brandValue").size(10));
        sourceBuilder.aggregation(brandAgg);

        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(10);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(10));
        sourceBuilder.aggregation(catalogAgg);

        //属性聚合
        NestedAggregationBuilder nestedAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder nestedAggregationBuilder = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        nestedAggregationBuilder.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        nestedAggregationBuilder.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        nestedAgg.subAggregation(nestedAggregationBuilder);
        sourceBuilder.aggregation(nestedAgg);


        sourceBuilder.query(query);
        System.out.println(sourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest(new String[]{"product"}, sourceBuilder);
        return searchRequest;

    }

    private SearchResult buildSearchResult() {
        return null;
    }
}
