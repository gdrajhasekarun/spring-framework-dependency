package com.home.learning.poc.springlib.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Keyword {

    public Keyword(String keywordName) {
        this.keywordName = keywordName;
        this.testData = new HashSet<>();
    }

    private String keywordName;
    private Set<String> testData;
}
