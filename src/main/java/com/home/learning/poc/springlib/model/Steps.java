package com.home.learning.poc.springlib.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Steps {

    public Steps(String keywordName, String annotationName) {
        this.keywordName = keywordName;
        this.annotationName=annotationName;
        this.testData = new HashSet<>();
    }

    private String keywordName;
    private String annotationName;
    private Set<String> testData;
}
