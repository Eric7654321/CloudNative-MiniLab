package com.minilab.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestTestController {

    @Autowired
    private TestController testController;
    @Test
    public void test() {
        String s = testController.testString();
        Assertions.assertEquals(s, "test");
    }
    @Test
    public void test2() {
        String s = testController.getTime();
        System.out.println(s);
    }
}
