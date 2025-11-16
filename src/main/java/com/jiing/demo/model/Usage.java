package com.jiing.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Usage {
    @JsonProperty("completion_tokens")
    private long completionTokens;

    @JsonProperty("prompt_tokens")
    private long promptTokens;

    @JsonProperty("total_tokens")
    private long totalTokens;

    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long value) { this.completionTokens = value; }

    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long value) { this.promptTokens = value; }

    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long value) { this.totalTokens = value; }
}

