package com.home.learning.poc.springlib.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PrefixProperties {
    @Value("${module.prefix:/default}")
    private String prefix;

}
