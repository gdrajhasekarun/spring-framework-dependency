package com.home.learning.poc.springlib.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
public class PrefixConfig implements WebMvcConfigurer {

    private final PrefixProperties prefixProperties;


    public PrefixConfig(PrefixProperties prefixProperties) {
        this.prefixProperties = prefixProperties;
    }
    @PostConstruct
    public void logPrefix() {
        System.out.println("Prefix: " + prefixProperties.getPrefix()); // Add a log or breakpoint here
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        if(StringUtils.hasText(this.prefixProperties.getPrefix())){
            configurer.addPathPrefix(this.prefixProperties.getPrefix(),
                    HandlerTypePredicate.forBasePackage("com.home.learning.poc.springlib.controller"));
        }
//        WebMvcConfigurer.super.configurePathMatch(configurer);
    }

//        @Override
//    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
//        return new RequestMappingHandlerMapping() {
//
//            @Override
//            protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
//                Class<?> beanType = method.getDeclaringClass();
//                if (beanType.getPackage().getName().startsWith("com.home.learning.poc.springlib.controller")) {
//                    String prefix = prefixProperties.getPrefix();
//                    PatternsRequestCondition original = mapping.getPatternsCondition();
//
//                    PatternsRequestCondition apiPattern = new PatternsRequestCondition(prefix);
//                    if (original != null) {
//                        apiPattern = apiPattern.combine(original);
//                    }
//
//                    mapping = new RequestMappingInfo(
//                            mapping.getName(),
//                            apiPattern,
//                            mapping.getMethodsCondition(),
//                            mapping.getParamsCondition(),
//                            mapping.getHeadersCondition(),
//                            mapping.getConsumesCondition(),
//                            mapping.getProducesCondition(),
//                            mapping.getCustomCondition());
//                }
//                super.registerHandlerMethod(handler, method, mapping);
//            }
//        };
//    }
}
