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

@SpringBootTest
class SeaerchApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
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
	class User{
    	String name;
		String address;
		int age;
	}
	@Test
	void indexSearch() throws Exception{
    	//首先构造request
		SearchRequest searchRequest = new SearchRequest("bank");
		//其次构造查询体
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//构造query
		searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
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
