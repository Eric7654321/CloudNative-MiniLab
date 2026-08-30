package com.minilab.controller;

import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.dto.AutoPlanRequest;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.service.AutoScheduleService;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private AutoScheduleService autoScheduleService;

    @DeleteMapping("/task/delete")
    public Result deleteTask(@RequestBody Task task) {
        log.info("刪除任務: {}", task);
        taskService.deleteTask(task);
        return Result.success();
    }

    @PutMapping("/task/update")
    public Result updateTask(@RequestBody Task task) {
        log.info("修改任務: {}", task);

        Result result = taskService.updateTask(task);
        if(result.getCode() != 1){
            return Result.error(result.getMsg());
        }
        Integer id = taskService.getTaskByEmpName(task.getEmpName()).getId();
        return Result.success(id);
    }

    /**
     * 自動排程：一批只寫了時段與所需技能的任務，回傳建議的人員與機台指派。
     * 這支只算不寫；組長在畫面上確認後仍走 /auto/ack 才會進資料庫。
     */
    @PostMapping("/auto/plan")
    public Result autoPlan(@RequestBody List<AutoPlanRequest> requests) {
        log.info("自動排程，共 {} 筆需求", requests == null ? 0 : requests.size());
        if (requests == null || requests.isEmpty()) {
            return Result.error("沒有待排程的任務");
        }
        return autoScheduleService.plan(requests.get(0).getGroup(), requests);
    }

    @PostMapping("/auto/ack")
    public Result TasksCheckAndAdd(@RequestBody List<Task> tasks) {
        log.info("進行任務可行性評估與排程，task: {}", tasks);
        Result result = taskService.tasksValidateAndCheck(tasks);
        if(result.getCode() != 1){
            log.info("任務評估與排程失敗，錯誤原因: {}", result.getMsg());
            return Result.error(result.getMsg());
        }
        return Result.success();
    }
}
