package com.minilab.controller;

import com.minilab.pojo.entity.Message;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.service.MessageService;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private MessageService messageService;

    @GetMapping("/search/{groupId}")
    public Result taskSearch(@PathVariable String groupId) {
        log.info("查詢tasks操作，groupId={}", groupId);
        List<Task> tasks = taskService.getTaskByGroupId(groupId);
        return Result.success(tasks);
    }

    @GetMapping("/check/weeks/{id}")
    public Result getTaskWeeks(@PathVariable Integer id) {
        log.info("根據員工id查詢兩個禮拜排程，id: {}", id);
        List<Task> tasks = taskService.getTaskByIdAndTimeWeeks(id, LocalDate.now());
        return Result.success(tasks);
    }

    @GetMapping("/check/today/{id}")
    public Result getTaskToday(@PathVariable Integer id) {
        log.info("根據員工id查詢今日排程，id: {}", id);
        List<Task> tasks = taskService.getTaskByIdAndTime(id, LocalDate.now());
        return Result.success(tasks);
    }

    @PostMapping("/msg/send")
    public Result reveiveMsg(@RequestBody Message message) {
        log.info("訊息接收，msg: {}", message);
        messageService.saveMessage(message);
        return Result.success();
    }

    @GetMapping("/msg/get/{groupId}")
    public Result getMsg(@PathVariable String groupId) {
        log.info("查詢訊息，groupId: {}", groupId);
        return Result.success(messageService.getMsgByGroupId(groupId));
    }


}
