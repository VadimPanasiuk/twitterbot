package com.vadim.twitterbot.entity;

import java.io.Serializable;

public class TweetContent implements Serializable {
    String searchParameter;

    public String getSearchParameter() {
        return searchParameter;
    }

    public void setSearchParameter(String searchParameter) {
        this.searchParameter = searchParameter;
    }
}
