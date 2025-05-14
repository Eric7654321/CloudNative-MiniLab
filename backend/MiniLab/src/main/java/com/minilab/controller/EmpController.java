package com.minilab.controller;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/emp")
public class EmpController {

    @Autowired
    private EmpService empService;

    @GetMapping("/search/{groupId}")
    public Result empSearch(@PathVariable String groupId) {
        log.info("查詢Emp操作，groupId={}", groupId);
        List<EmpVO> EmpVOs = empService.getEmpByGroupId(groupId);
        return Result.success(EmpVOs);
    }
}
