package com.jiing.demo.service.Impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jiing.demo.constants.CivilCodeConstants;
import com.jiing.demo.model.ApifoxModel;
import com.jiing.demo.model.ChatResponseModel;
import com.jiing.demo.model.Message;
import com.jiing.demo.service.AskSearchService;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;  // Import OkHttpClient
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class AskSearchServiceImpl implements AskSearchService {

    private static final Integer TIME_OUT = 5 * 60;

    private RestHighLevelClient restHighLevelClient;

    public AskSearchServiceImpl(RestHighLevelClient client) {
        restHighLevelClient = client;
    }
    
    @Override
    public List<String> searchCivilCode(String query) {
        List<String> highlights = new ArrayList<>();
        try {
            highlights = searchByAllField(query);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return highlights;
    }

    List<String> searchByAllField(String query) throws IOException {
        List<String> highlights = new ArrayList<>();
        // 1. 创建搜索请求
        SearchRequest searchRequest = new SearchRequest(CivilCodeConstants.INDEX_NAME);

        // 2. 构建查询条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("code_content", query));
        
        sourceBuilder.highlighter(new HighlightBuilder().field("code_content")
                .requireFieldMatch(false)
                .preTags("<em>")  // 高亮前缀
                .postTags("</em>")
                .numOfFragments(0)); 

        // 3. 设置查询条件到请求中
        searchRequest.source(sourceBuilder);

        // 4. 发送请求
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 5. 解析响应
        System.out.println("匹配的文档总数: " + response.getHits().getTotalHits().value);
        System.out.println("Search Results:");
       
        response.getHits().forEach(hit -> {
            // 获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 根据字段名获取高亮结果
                HighlightField highlightField = highlightFields.get("code_content");
                if (highlightField != null) {
                    // 获取高亮值
                    String highlightedCodeContent = highlightField.getFragments()[0].string();
                    highlights.add(highlightedCodeContent);
                }
            }
        });
        return highlights;
    }

        /**
     * 整合ES搜索和AI回答的方法
     * 先从ES搜索相关法律条文，然后将搜索结果作为上下文发送给AI进行解答
     * 
     * @param query 用户的查询问题
     * @return SseEmitter 流式返回AI的回答
     */
    public SseEmitter searchAndAskAI(String query) {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                // 1. 先从ES搜索相关法律条文
                List<String> searchResults = searchByAllField(query);
                
                // 2. 构建包含搜索结果的提示词
                StringBuilder context = new StringBuilder();
                context.append("根据以下相关法律条文，请回答用户的问题。\n\n");
                context.append("相关法律条文：\n");
                
                if (!searchResults.isEmpty()) {
                    for (int i = 0; i < searchResults.size(); i++) {
                        context.append(i + 1).append(". ").append(searchResults.get(i)).append("\n\n");
                    }
                } else {
                    context.append("未找到相关法律条文。\n\n");
                }
                
                context.append("用户问题：").append(query).append("\n\n");
                context.append("请基于以上法律条文给出专业的解答。");
                
                // 3. 调用AI进行回答
                streamDataWithContext(context.toString(), emitter);
                
            } catch (Exception e) {
                emitter.completeWithError(e);
                e.printStackTrace();
            }
        }).start();

        return emitter;
    }

    /**
     * 内部方法：使用指定的文本内容和emitter进行流式AI请求
     */
    private void streamDataWithContext(String text, SseEmitter emitter) {
        OkHttpClient client = new OkHttpClient();

        try {
            MediaType mediaType = MediaType.parse("application/json");

            // Create the ApifoxModel and set fields
            ApifoxModel apifoxModel = new ApifoxModel();
            
            // Assuming 'Message' class exists, initialize it accordingly
            Message message = new Message();
            message.setContent(text); // Example to set the text content
            message.setRole("user"); // Define role or any other relevant fields

            // Set messages
            apifoxModel.setMessages(new Message[]{ message });
            apifoxModel.setMaxTokens(1000L); // Example values
            apifoxModel.setModel("gpt-3.5-turbo"); // Your model of choice
            apifoxModel.setStream(true); // Enable streaming if needed

            // Convert ApifoxModel to JSON
            Gson gson = new Gson();
            String jsonRequestBody = gson.toJson(apifoxModel);

            RequestBody body = RequestBody.create(mediaType, jsonRequestBody);

            // 构建请求
            Request request = new Request.Builder()
                .url("https://**/v1/chat/completions")
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "**")
                .addHeader("Content-Type", "application/json")
                .build();

            // 发送请求
            Response response = client.newCall(request).execute();
            ObjectMapper objectMapper = new ObjectMapper();

            // 读取流式响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    line = line.substring(5).trim();
                }
                if ("[DONE]".equals(line) || line.isEmpty()) {
                    continue;
                }

                // 使用ObjectMapper解析JSON
                try {
                    ChatResponseModel chatResponse = objectMapper.readValue(line, ChatResponseModel.class);
                    // 输出结果
                    System.out.println("ID: " + chatResponse.getid());
                    System.out.println("Object: " + chatResponse.getObject());
                    System.out.println("Created: " + chatResponse.getCreated());
                    // 将每行数据发送给前端
                    emitter.send(chatResponse.getChoices()[0].getDelta().getContent());
                } catch (Exception e) {
                    // 处理解析错误
                    System.out.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
            emitter.complete();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    @Override
    public SseEmitter streamData(String text) {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            try {
               
                MediaType mediaType = MediaType.parse("application/json");
                //RequestBody body = RequestBody.create(mediaType, "{\n    \"model\": \"gpt-3.5-turbo\",\n    \"stream\": true,\n    \"messages\": [\n      {\n        \"role\": \"system\",\n        \"content\": \"You are a helpful assistant.\"\n      },\n      {\n        \"role\": \"user\",\n        \"content\": \"Hello!\"\n      }\n    ]\n  }");

                // Create the ApifoxModel and set fields
                ApifoxModel apifoxModel = new ApifoxModel();
                
                // Assuming 'Message' class exists, initialize it accordingly
                Message message = new Message();
                message.setContent(text); // Example to set the text content
                message.setRole("user"); // Define role or any other relevant fields

                // Set messages
                apifoxModel.setMessages(new Message[]{ message });
                apifoxModel.setMaxTokens(1000L); // Example values
                apifoxModel.setModel("gpt-3.5-turbo"); // Your model of choice
                apifoxModel.setStream(true); // Enable streaming if needed

                // Convert ApifoxModel to JSON
                Gson gson = new Gson();
                String jsonRequestBody = gson.toJson(apifoxModel);

                RequestBody body = RequestBody.create(mediaType, jsonRequestBody);


                // 构建请求
                Request request = new Request.Builder()
                    .url("https://**/v1/chat/completions") // 替换为实际的 OpenAI API endpoint
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer **") // 使用你的 OpenAI API Key
                    .addHeader("Content-Type", "application/json")
                    .build();

                // 发送请求
                Response response = client.newCall(request).execute();
                ObjectMapper objectMapper = new ObjectMapper();

                // 读取流式响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        line = line.substring(5).trim();
                    }
                    if ("[DONE]".equals(line) || line.isEmpty()) {
                        continue;
                    }
                   
                    
                    // 使用ObjectMapper解析JSON
                    try {
                        ChatResponseModel chatResponse = objectMapper.readValue(line, ChatResponseModel.class);
                        // 输出结果
                        System.out.println("ID: " + chatResponse.getid());
                        System.out.println("Object: " + chatResponse.getObject());
                        System.out.println("Created: " + chatResponse.getCreated());
                        // 将每行数据发送给前端
                        emitter.send(chatResponse.getChoices()[0].getDelta().getContent());
                    } catch (Exception e) {
                        // 处理解析错误
                        System.out.println("Error parsing line: " + line);
                        e.printStackTrace();
                    }
                }
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}

