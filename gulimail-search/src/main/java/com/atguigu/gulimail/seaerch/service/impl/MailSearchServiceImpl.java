package com.atguigu.gulimail.seaerch.service.impl;

import com.atguigu.gulimail.seaerch.config.ElasticSearchConfig;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        //must模糊匹配skuName
        if (!StringUtils.isEmpty(param.getKeyword())) {
            query.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
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
        }
        //根据属性查询
        if (!StringUtils.isEmpty(param.getAttrs())) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (String attr : param.getAttrs()) {
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一对都得生成一个nestedquery
                query.filter(QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None));
            }
        }
        searchSourceBuilder.query(query);


        SearchRequest searchRequest = new SearchRequest(new String[]{"product"}, searchSourceBuilder);
        return searchRequest;

    }

    private SearchResult buildSearchResult() {
    }
}
