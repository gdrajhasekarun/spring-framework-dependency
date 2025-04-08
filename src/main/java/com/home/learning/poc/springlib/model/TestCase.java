package com.home.learning.poc.springlib.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TestCase {


    private String testCaseName;
    private List<String> keywordsList;
    private Map<String, String> testData;

}
