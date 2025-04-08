package com.home.learning.poc.springlib.testdata;

import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;
@Setter
@Component
public class TestDataProvider {

    private Map<String, String > dataProvider;

    public String getData(String sheet, String key){
        return dataProvider.get(key);
    }
}
