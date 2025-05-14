package com.minilab.controller;

import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.MachineService;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping("/search/{groupId}")
    public Result taskSearch(@PathVariable String groupId) {
        log.info("查詢tasks操作，groupId={}", groupId);
        List<Task> tasks = taskService.getTaskByGroupId(groupId);
        return Result.success(tasks);
    }
}
