package com.jiing.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import com.jiing.demo.constants.CivilCodeConstants;
import com.jiing.demo.thirdparty.FileRegexMatcher;

public class ElasticCivilCodeTest {
 private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://127.0.0.1:9200")
        ));
    }


    // 运行下面2个步骤插入民法数据到ES
    /**
     * STEP 1 运行 createCivilCodeIndex 在ES中创建民法典INDEX
     * @throws IOException
     */
    @Test
    void createCivilCodeIndex() throws IOException {
        // 1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest(CivilCodeConstants.INDEX_NAME);
        // 2.准备请求的参数：DSL语句
        request.source(CivilCodeConstants.MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * STEP 2 运行 indexCivilCodeData 在ES中创建民法典DOCs
     * @throws IOException
     */
    @Test
    void indexCivilCodeData() throws IOException {
        // 1. 准备批量请求对象
        BulkRequest bulkRequest = new BulkRequest();

        // 2. 读取民法典条目
        List<String> civilCodes = FileRegexMatcher.readCivilCode();

        // 3. 遍历条目，逐条添加到批量请求中
        for (String code : civilCodes) {
            Map<String, Object> civilCodeData = new HashMap<>();
            civilCodeData.put("code_content", code);

            // 创建单条文档的 IndexRequest
            IndexRequest indexRequest = new IndexRequest(CivilCodeConstants.INDEX_NAME)
                    .source(civilCodeData, XContentType.JSON);

            // 添加到 BulkRequest
            bulkRequest.add(indexRequest);
        }

        // 4. 发送批量请求
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        // 5. 输出结果
        if (!bulkResponse.hasFailures()) {
            System.out.println("所有文档批量添加成功！");
        } else {
            System.err.println("部分文档添加失败，失败原因：");
            System.err.println(bulkResponse.buildFailureMessage());
        }
    }

    @Test
    void testDeleteCivilCodeIndex() throws IOException {
        // 1.创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest(CivilCodeConstants.INDEX_NAME);
        // 2.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void searchByAllField() throws IOException {
        // 1. 创建搜索请求
        SearchRequest searchRequest = new SearchRequest(CivilCodeConstants.INDEX_NAME);

        // 2. 构建查询条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("code_content", "延边某某置业有限公司"));
        
        sourceBuilder.highlighter(new HighlightBuilder().field("code_content").requireFieldMatch(false)
                .preTags("<em>")  // 高亮前缀
                .postTags("</em>"));


        // 3. 设置查询条件到请求中
        searchRequest.source(sourceBuilder);

        // 4. 发送请求
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        // 5. 解析响应
        System.out.println("匹配的文档总数: " + response.getHits().getTotalHits().value);
        System.out.println("Search Results:");
        response.getHits().forEach(hit -> {
            //System.out.println(hit.getSourceAsString());
             // 获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get("code_content");
                if (highlightField != null) {
                    // 获取高亮值
                    String highlightedCodeContent = highlightField.getFragments()[0].string();
                    System.out.println(highlightedCodeContent);
                }
            }
        });
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
