package com.home.learning.poc.springlib.controller;

import com.home.learning.poc.springlib.model.Action;
import com.home.learning.poc.springlib.model.Keyword;
import com.home.learning.poc.springlib.model.Steps;
import com.home.learning.poc.springlib.model.TestCase;
import com.home.learning.poc.springlib.service.TestCaseExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AppController {

    TestCaseExecutor testCaseExecutor;
    public AppController(TestCaseExecutor testCaseExecutor){
        this.testCaseExecutor = testCaseExecutor;
    }

    @GetMapping("/action-list-aop")
    public ResponseEntity<List<Action>> getAllActionsAop(@RequestParam(name = "applications", required = false) List<String> applications) throws Exception {
        return ResponseEntity.ok(this.testCaseExecutor.getAllActions(applications));
    }













    @PostMapping("/run-testcase")
    public ResponseEntity<String> runTestCases(@RequestBody TestCase testCase) {
        String status = "Passed";
        HttpStatus httpStatus;
        try{
            this.testCaseExecutor.executeTestcases(testCase.getKeywordsList(), testCase.getTestData());
            httpStatus = HttpStatus.OK;
        }catch (Exception e){
            httpStatus = HttpStatus.BAD_REQUEST;
            status = "Failed";
        }
        return ResponseEntity.status(httpStatus).body(status);
    }

    @GetMapping("/keyword-list")
    public ResponseEntity<List<Keyword>> getAllKeywords() throws Exception {
        return ResponseEntity.ok(this.testCaseExecutor.getAllKeywords());
    }

    @GetMapping("/keyword-list-aop")
    public ResponseEntity<List<Keyword>> getAllKeywordsAop() throws Exception {
        return ResponseEntity.ok(this.testCaseExecutor.getAllKeywordsWithoutAnnotation());
    }

    @GetMapping("/steps-list-aop")
    public ResponseEntity<List<Steps>> getAllStepsAop() throws Exception {
        return ResponseEntity.ok(this.testCaseExecutor.getAllKeywordsWithTestSteps());
    }


}
