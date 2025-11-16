package com.jiing.demo.service;

import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AskSearchService {

    List<String> searchCivilCode(String query);

    public SseEmitter streamData(String text);

    public SseEmitter searchAndAskAI(String query);

} 
