package com.minilab.controller;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.EmpService;
import com.minilab.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
