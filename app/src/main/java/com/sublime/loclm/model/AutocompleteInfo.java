package com.sublime.loclm.model;

public class AutocompleteInfo {
    private String primaryText;
    private String secondaryText;
    private String id;


    public String getPrimaryText() {
        return primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}