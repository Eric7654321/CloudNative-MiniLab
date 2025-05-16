package com.minilab.controller;

import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private TaskService taskService;

    @DeleteMapping("/task/delete")
    public Result deleteTask(@RequestBody Task task) {
        log.info("刪除任務: {}", task);
        taskService.deleteTask(task);
        return Result.success();
    }

    @PutMapping("/task/update")
    public Result updateTask(@RequestBody Task task) {
        log.info("修改任務: {}", task);
        taskService.updateTask(task);
        return Result.success();
    }
}
