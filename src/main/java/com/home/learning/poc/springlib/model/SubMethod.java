package com.home.learning.poc.springlib.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubMethod {

    private Class<?> className;
    private String methodName;
}
