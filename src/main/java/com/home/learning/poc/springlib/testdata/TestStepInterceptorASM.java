package com.home.learning.poc.springlib.testdata;

import com.home.learning.poc.springlib.annotation.TestSteps;
import com.home.learning.poc.springlib.model.Steps;
import com.home.learning.poc.springlib.model.SubMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class TestStepInterceptorASM {

    TestDataInterceptorASM testDataInterceptorASM;

    public TestStepInterceptorASM(TestDataInterceptorASM testDataInterceptorASM){
        this.testDataInterceptorASM = testDataInterceptorASM;
    }


    public void analyzeTestSteps(Class<?> clazz, List<Steps> stepsList) throws IOException {
        for (Method method : clazz.getDeclaredMethods()) {
            TestSteps annotation = method.getAnnotation(TestSteps.class);
            if (annotation != null) {
                Steps steps = new Steps(method.getName(), annotation.value());
                String packageName = clazz.getPackage().getName();
                packageName = packageName.substring(0, packageName.lastIndexOf("."));
                Set<String> dataKeys = analyzeMethodForDataKeys(method, clazz, packageName);
                steps.setTestData(dataKeys);
                stepsList.add(steps);
            }
        }
    }


    private Set<String> analyzeMethodForDataKeys(Method annotatedMethod, Class<?> clazz, String packageName) throws IOException {
        Set<String> allDataKeys = new HashSet<>();

        // First analyze the method itself
        Set<String> directDataKeys = this.testDataInterceptorASM.analyzeMethod(clazz, annotatedMethod.getName());
        allDataKeys.addAll(directDataKeys);

        // Then analyze any methods called within this method
        List<SubMethod> calledMethodNames = this.testDataInterceptorASM.extractMethodCalls(annotatedMethod.getName(), clazz,
                packageName.replace('.', '/'));
        for (SubMethod calledMethodName : calledMethodNames) {
            allDataKeys.addAll(this.testDataInterceptorASM.analyzeMethod(calledMethodName.getClassName(), calledMethodName.getMethodName()));
        }

        return allDataKeys;
    }



}
