package com.jiing.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.jiing.demo.service.AskSearchService;

@RestController
@RequestMapping("/api")  // Adding /api prefix here
public class SearchController {

    @Autowired
    AskSearchService askSearchService;
    
    @PostMapping("/searchCivilCode")
    public List<String> searchCivilCode(@RequestBody String query) {
        List<String> civilCodeList = askSearchService.searchCivilCode(query);
        return civilCodeList;
    }

    @PostMapping("/streamData")
    public SseEmitter streamData(@RequestBody String query) {
        return askSearchService.searchAndAskAI(query);
    }


}
