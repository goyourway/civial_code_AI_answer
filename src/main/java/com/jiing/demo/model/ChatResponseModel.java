package com.jiing.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponseModel {
    private Choice[] choices;
    private long created;
    private String id;
    private String object;
    private Usage usage;
    private String model;


    public Choice[] getChoices() { return choices; }
    public void setChoices(Choice[] value) { this.choices = value; }

    public long getCreated() { return created; }
    public void setCreated(long value) { this.created = value; }

    public String getid() { return id; }
    public void setid(String value) { this.id = value; }

    public String getObject() { return object; }
    public void setObject(String value) { this.object = value; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage value) { this.usage = value; }

        
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
}
