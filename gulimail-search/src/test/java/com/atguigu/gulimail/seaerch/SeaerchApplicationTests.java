package com.atguigu.gulimail.seaerch;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimail.seaerch.config.ElasticSearchConfig;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Array;

@SpringBootTest
class SeaerchApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Test
    void contextLoads() {
        Object[] a = shift(new Integer[]{1, 2, 3}, Integer.class);
        for (Object integer : a) {
            System.out.println(integer);
        }
    }

    public <T> T[] shift(T[] arr, Class<T> type) {
//        T[] arr1 = (T[]) Array.newInstance(type, arr.length + 1);
        T[] arr1 = (T[]) new Object[arr.length+1];
        for (int i = arr.length; i > 0; i--) {
            arr1[i] = arr[i - 1];
        }
//        for (int i = 0; i < a.length; i++) {
//            b[i] = a[i];
//        }

        return arr1;
    }

    public <T> void test2(T[] a) {
        T[] b = (T[]) new Object[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }

        for (int i = 0; i < b.length; i++) {
            System.out.println(b[i]);
        }
    }

    @Test
    void test3() {
        Object[] array1 = new Integer[]{1,2,3};
        Integer[] i1 = (Integer[]) array1;

    }

    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("user");
        indexRequest.id("1");
        User user = new User();
        String s = JSON.toJSONString(user);
        indexRequest.source(s, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);

    }

    class User {
        String name;
        String address;
        int age;
    }

    @Test
    void indexSearch() throws Exception {
        //首先构造request
        SearchRequest searchRequest = new SearchRequest("bank");
        //其次构造查询体
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构造query
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        System.out.println(searchSourceBuilder);
        //构造aggregation
        searchSourceBuilder.aggregation(AggregationBuilders.terms("age_group").field("age").size(10));
        System.out.println(searchSourceBuilder);

        //最后发出请求
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response);

        Terms age_group = response.getAggregations().get("age_group");
        for (Terms.Bucket bucket : age_group.getBuckets()) {
            System.out.println(bucket.getKeyAsString());
        }


    }
}
