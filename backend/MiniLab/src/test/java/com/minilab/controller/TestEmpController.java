package com.minilab.controller;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.EmpVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void testInsertEmpAndDeleteEmp() {
        log.info("測試新增與移除員工是否正常");
        log.info("新增員工");

        Emp emp = new Emp();
        emp.setUsername("test");
        emp.setName("test");
        emp.setPassword("123456");
        emp.setGroup("114514");
        emp.setRole(0);
        emp.setUsable(0);
        Result result = empController.insertEmp(emp);
        Assertions.assertEquals(result.getCode(), 1);

        log.info("移除員工");
        Emp deleteEmp = new Emp();
        BeanUtils.copyProperties(result.getData(), deleteEmp);
        Result result1 = empController.deleteEmp(deleteEmp);
        Assertions.assertEquals(result1.getCode(), 1);
    }

    @Test
    public void testSearchAndUpdateEmp() {
        log.info("測試查詢與修改員工操作是否正常");

        log.info("查詢操作");
        Result result = empController.empSearch("114514");
        Assertions.assertEquals(result.getCode(), 1);

        @SuppressWarnings("unchecked")
        List<EmpVO> emps = (List<EmpVO>) result.getData();
        log.info("查詢結果:{}", emps);
        Assertions.assertFalse(emps.isEmpty(), "查詢結果為空");

        log.info("修改操作");
        Emp emp = new Emp();
        BeanUtils.copyProperties(emps.get(0), emp);
        emp.setPassword("Wally114514");

        Result result1 = empController.updateEmp(emp);
        Assertions.assertEquals(result1.getCode(), 1);
    }

    @Test
    public void testEmpTagUpdate() {
        EmpTag empTag = new EmpTag();
        empTag.setEmpId(1);
        empTag.setEmpId(1);
        empTag.setTags("[\"物性\",\"電性\",\"獸性\",\"野性\",\"鹼性\",\"弱酸性\"]");

        Result result = empController.updateEmpTag(empTag);
        Assertions.assertEquals(result.getCode(), 1);
    }

}
