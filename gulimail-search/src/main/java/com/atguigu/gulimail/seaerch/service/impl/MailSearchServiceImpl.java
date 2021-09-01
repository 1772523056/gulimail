package com.atguigu.gulimail.seaerch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.AttrResVo;
import com.atguigu.gulimail.seaerch.config.ElasticSearchConfig;
import com.atguigu.gulimail.seaerch.constant.EsConstant;
import com.atguigu.gulimail.seaerch.feign.AttrsFeignService;
import com.atguigu.gulimail.seaerch.service.MailSearchService;
import com.atguigu.gulimail.seaerch.vo.SearchParam;
import com.atguigu.gulimail.seaerch.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MailSearchServiceImpl implements MailSearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    AttrsFeignService attrsFeignService;

    @Override
    public SearchResult listPage (SearchParam searchParam) {
        //todo 动态构建出需要的dsl语句
        SearchResult searchResult = null;


        //准备检索请求
        SearchRequest searchRequest = buidSeaerchRequest (searchParam);

        try {
            SearchResponse response = restHighLevelClient.search (searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //todo 解析返回的json 封装成vo
            searchResult = buildSearchResult (response, searchParam);
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return searchResult;
    }

    private SearchRequest buidSeaerchRequest (SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder ();
        BoolQueryBuilder query = QueryBuilders.boolQuery ();
        //todo 1. 查询 query
        //todo 1.1 关键字模糊匹配 must
        if (!StringUtils.isEmpty (param.getKeyword ())) {
            query.must (QueryBuilders.matchQuery ("skuTitle", param.getKeyword ()));
        }
        //todo 1.2 根据选择过滤 filter
        //过滤三级分类id
        if (param.getCatalog3Id () != null) {
            query.filter (QueryBuilders.termQuery ("catalogId", param.getCatalog3Id ()));
        }
        //过滤品牌id
        if (param.getBrandId () != null && param.getBrandId ().size () > 0) {
            query.filter (QueryBuilders.termsQuery ("brandId", param.getBrandId ().toArray (new Long[0])));
        }
        //按照是否有库存进行查询
//        query.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        //按照价格范围进行查询
        String skuPrice = param.getSkuPrice ();
        if (!StringUtils.isEmpty (skuPrice)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery ("skuPrice");
            String[] s = skuPrice.split ("_");
            if (s.length == 2) {
                rangeQuery.gte (s[0]).lte (s[1]);
            } else if (s.length == 1) {
                if (skuPrice.startsWith ("_")) {
                    rangeQuery.lte (s[0]);
                }
                if (skuPrice.endsWith ("_")) {
                    rangeQuery.gte (s[0]);
                }
            }
            query.filter (rangeQuery);
        }

        //根据属性查询
        if (!StringUtils.isEmpty (param.getAttrs ())) {
            for (String attr : param.getAttrs ()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery ();
                String[] s = attr.split ("_");
                String attrId = s[0];
                String[] attrValues = s[1].split (":");
                boolQueryBuilder.must (QueryBuilders.termQuery ("attrs.attrId", attrId));
                boolQueryBuilder.must (QueryBuilders.termsQuery ("attrs.attrValue", attrValues));
                //每一对都得生成一个nestedquery
                query.filter (QueryBuilders.nestedQuery ("attrs", boolQueryBuilder, ScoreMode.None));
            }
        }
        //todo 2.排序 sort
        //根据升序或降序排序
        if (!StringUtils.isEmpty (param.getSort ())) {
            String[] s = param.getSort ().split ("_");
            SortOrder order = s[1].equalsIgnoreCase ("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort (s[0], order);
        }

        sourceBuilder.from ((int) ((param.getPageNumber () - 1) * EsConstant.PRODUCT_PAGESIZE));
        sourceBuilder.size (Math.toIntExact (EsConstant.PRODUCT_PAGESIZE));
        //todo 3.高亮 highlight
        //高亮
        if (!StringUtils.isEmpty (param.getKeyword ())) {
            HighlightBuilder builder = new HighlightBuilder ();
            builder.field ("skuTitle");
            builder.preTags ("<b style='color:red'>");
            builder.postTags ("</b>");
            sourceBuilder.highlighter (builder);
        }


        //todo 4.聚合 aggregations
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms ("brand_agg");
        brandAgg.field ("brandId").size (10);
        brandAgg.subAggregation (AggregationBuilders.terms ("brand_name_agg").field ("brandName").size (10));
        brandAgg.subAggregation (AggregationBuilders.terms ("brand_img_agg").field ("brandImg").size (10));
        sourceBuilder.aggregation (brandAgg);

        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms ("catalog_agg");
        catalogAgg.field ("catalogId").size (10);
        catalogAgg.subAggregation (AggregationBuilders.terms ("catalog_name_agg").field ("catalogName").size (10));
        sourceBuilder.aggregation (catalogAgg);

        //属性聚合
        NestedAggregationBuilder nestedAgg = AggregationBuilders.nested ("attr_agg", "attrs");
        TermsAggregationBuilder nestedAggregationBuilder = AggregationBuilders.terms ("attr_id_agg").field ("attrs.attrId").size (10);
        nestedAggregationBuilder.subAggregation (AggregationBuilders.terms ("attr_name_agg").field ("attrs.attrName").size (10));
        nestedAggregationBuilder.subAggregation (AggregationBuilders.terms ("attr_value_agg").field ("attrs.attrValue").size (10));
        nestedAgg.subAggregation (nestedAggregationBuilder);
        sourceBuilder.aggregation (nestedAgg);


        sourceBuilder.query (query);
        System.out.println (sourceBuilder.toString ());
        SearchRequest searchRequest = new SearchRequest (new String[]{"product"}, sourceBuilder);
        return searchRequest;

    }

    private SearchResult buildSearchResult (SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult ();
        //设置总记录数
        SearchHits hits = response.getHits ();
        long value = hits.getTotalHits ().value;
        result.setTotal (value);
        result.setPageNumber (param.getPageNumber ());

        //设置总页数
        Long totalPages = value % EsConstant.PRODUCT_PAGESIZE == 0 ? (value / EsConstant.PRODUCT_PAGESIZE) : (value / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages (totalPages);

        //设置查询到的商品数据
        SearchHit[] hitsHits = hits.getHits ();
        List<SkuEsModel> productList = Arrays.asList (hitsHits).stream ().map (ele -> {
            String s = ele.getSourceAsString ();
            SkuEsModel esModel = JSON.parseObject (s, SkuEsModel.class);
            if (!StringUtils.isEmpty (param.getKeyword ())) {
                HighlightField skuTitle = ele.getHighlightFields ().get ("skuTitle");
                String string = skuTitle.getFragments ()[0].string ();
                esModel.setSkuTitle (string);
            }
            return esModel;
        }).collect (Collectors.toList ());
        result.setProducts (productList);

        //设置分类vo
        ParsedLongTerms catalog_agg = response.getAggregations ().<ParsedLongTerms>get ("catalog_agg");
        List<SearchResult.CatalogVo> catalogVoList = catalog_agg.getBuckets ().stream ().map (ele -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo ();
            String key = ele.getKeyAsString ();
            //得到分类id
            catalogVo.setCatalogId (Long.parseLong (key));
            ParsedStringTerms name = ele.getAggregations ().get ("catalog_name_agg");
            String keyAsString = name.getBuckets ().get (0).getKeyAsString ();
            catalogVo.setCatalogName (keyAsString);
            return catalogVo;
        }).collect (Collectors.toList ());
        result.setCatalogs (catalogVoList);

        //设置品牌vo
        ParsedLongTerms brand_agg = response.getAggregations ().get ("brand_agg");
        List<SearchResult.BrandVo> brandVoList = brand_agg.getBuckets ().stream ().map (ele -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo ();
            String key = ele.getKeyAsString ();
            brandVo.setBrandId (Long.parseLong (key));

            String brandName = ((ParsedStringTerms) ele.getAggregations ().get ("brand_name_agg")).getBuckets ().get (0).getKeyAsString ();
            brandVo.setBrandName (brandName);

            String img = ((ParsedStringTerms) ele.getAggregations ().get ("brand_img_agg")).getBuckets ().get (0).getKeyAsString ();
            brandVo.setBrandImg (img);
            return brandVo;
        }).collect (Collectors.toList ());
        result.setBrands (brandVoList);

        //设置属性vo
        ParsedNested attr_agg = response.getAggregations ().get ("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations ().get ("attr_id_agg");
        List<SearchResult.AttrVo> attrVoList = attr_id_agg.getBuckets ().stream ().map (ele -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo ();
            attrVo.setAttrId (Long.parseLong (ele.getKeyAsString ()));
            ParsedStringTerms attr_name_agg = ele.getAggregations ().get ("attr_name_agg");
            String s = attr_name_agg.getBuckets ().get (0).getKeyAsString ();
            attrVo.setAttrName (s);
            Stream<String> attrvalueList = ((ParsedStringTerms) ele.getAggregations ().get ("attr_value_agg")).getBuckets ().stream ().map (e -> e.getKeyAsString ());
            attrVo.setAttrValue (attrvalueList.collect (Collectors.toList ()));
            return attrVo;
        }).collect (Collectors.toList ());
        result.setAttrs (attrVoList);

        //设置页数vo
        List<Integer> pageNavs = new ArrayList<> ();
        for (int i = 0; i < totalPages; i++) {
            pageNavs.add (i);
        }
        result.setPageNavs (pageNavs);

        //设置面包屑vo
        if (param.getAttrs () != null && param.getAttrs ().size () > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs ().stream ().map (ele -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo ();
                String[] s = ele.split ("_");
                navVo.setNaveValue (s[1]);
                Long id = Long.parseLong (s[0]);
                R r = attrsFeignService.info (id);

                if (r.getCode () == 0) {
                    AttrResVo attr = r.getData ("attr", new TypeReference<AttrResVo> () {
                    });
                    navVo.setNavName (attr.getAttrName ());
                } else {
                    navVo.setNavName (s[0]);
                }

                //取消面包屑后 应该把当前的url的attr删掉
                String encode = null;
                try {
                    encode = URLEncoder.encode (ele, "UTF-8");
                    encode.replace ("+","%20");//浏览器对空格的编码和java不一样
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace ();
                }
//                if(param.get_queryString().s)
                String replace = null;
                String queryString = param.get_queryString ();
                if (queryString.contains ("&attrs=" + encode))
                    replace = queryString.replace ("&attrs=" + encode, "");
                else
                    replace = queryString.replace ("attrs=" + encode, "");
                navVo.setLink ("http://search.xzb.com/list.html?" + replace);
                return navVo;
            }).collect (Collectors.toList ());

            result.setNavs (collect);
        }

        return result;
    }
}
