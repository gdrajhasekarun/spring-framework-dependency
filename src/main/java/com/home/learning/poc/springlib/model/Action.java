package com.home.learning.poc.springlib.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
@Data
public class Action {

    public Action(String className, String keywordName) {
        this.className = className;
        this.keywordName = keywordName;
        this.testData = new HashSet<>();
    }

    private String className;
    private String keywordName;
    private Set<String> testData;
}
