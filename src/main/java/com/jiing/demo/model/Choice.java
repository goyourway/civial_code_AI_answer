package com.jiing.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {
    private String finishReason;
    private Long index;
    private Delta delta;

    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String value) { this.finishReason = value; }

    public Long getIndex() { return index; }
    public void setIndex(Long value) { this.index = value; }

    public Delta getDelta() { return delta; }
    public void setDelta(Delta value) { this.delta = value; }
}