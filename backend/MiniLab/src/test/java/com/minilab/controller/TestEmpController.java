package com.minilab.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TestEmpController {

    @Autowired
    private EmpController empController;

    @Test
    public void testEmpSearch() {
        log.info("測試員工查詢是否正常");
        System.out.println(empController.empSearch("114514"));
    }

}
