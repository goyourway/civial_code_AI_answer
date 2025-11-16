package com.jiing.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Delta {
    private String content;
 

    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }

}