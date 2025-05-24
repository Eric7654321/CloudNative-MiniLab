package com.minilab.controller;

import com.minilab.pojo.entity.*;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.EmpService;
import com.minilab.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/machine")
public class MachineController {

    @Autowired
    private MachineService machineService;

    @GetMapping("/search/{groupId}")
    public Result machineSearch(@PathVariable String groupId) {
        log.info("查詢Machine操作，groupId={}", groupId);
        List<MachineVO> machines = machineService.getMachineByGroupId(groupId);
        return Result.success(machines);
    }

    @PostMapping("/insert")
    public Result insertMachine(@RequestBody Machine machine) {
        log.info("新增機器操作，Emp={}", machine);
        machineService.insert(machine);
        return Result.success();
    }

    @PutMapping("/tag/update")
    public Result updateMachineTag(@RequestBody MachineTag tag) {
        log.info("修改Tag操作，Tag={}", tag);
        Result result = machineService.updateTag(tag);
        if (result.getCode() == 0) {
            return Result.error(result.getMsg());
        }

        return Result.success();
    }

    @PutMapping("/update")
    public Result updateMachine(@RequestBody Machine machine) {
        log.info("修改機器資訊: {}", machine);
        machineService.updateMachine(machine);

        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result deleteMachine(@RequestBody Machine machine) {
        log.info("刪除機器資訊: {}", machine);
        machineService.deleteMachine(machine);

        return Result.success();
    }

}
