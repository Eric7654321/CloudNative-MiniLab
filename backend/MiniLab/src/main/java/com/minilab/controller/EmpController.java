package com.minilab.controller;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/insert")
    public Result insertEmp(@RequestBody Emp emp) {
        log.info("新增Emp操作，Emp={}", emp);
        empService.insert(emp);

        emp = empService.seleteEmpByUsername(emp.getUsername());
        return Result.success(emp);
    }

    @PutMapping("/tag/update")
    public Result updateEmpTag(@RequestBody EmpTag tag) {
        log.info("修改Tag操作，Tag={}", tag);
        Result result = empService.updateTag(tag);
        if (result.getCode() == 0) {
            return Result.error(result.getMsg());
        }

        return Result.success();
    }

    @PutMapping("/update")
    public Result updateEmp(@RequestBody Emp emp) {
        log.info("修改工人資訊: {}", emp);
        empService.updateEmp(emp);

        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result deleteEmp(@RequestBody Emp emp) {
        log.info("刪除工人資訊: {}", emp);
        empService.deleteEmp(emp);

        return Result.success();
    }
}
