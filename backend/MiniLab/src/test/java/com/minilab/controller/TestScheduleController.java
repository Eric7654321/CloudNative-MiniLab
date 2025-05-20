package com.minilab.controller;

import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootTest
@Slf4j
public class TestScheduleController {

    @Autowired
    private ScheduleController scheduleController;

    @Test
    public void testAddUpdateDeleteTask() {
        log.info("測試新增、修改與刪除任務流程");

        // === 1. 新增任務 ===
        Task newTask = new Task();
        newTask.setEmp(1);  // 預設測試資料中有 empId = 1
        newTask.setEmpName("測試用員工");
        newTask.setMachine("[\"1\"]");
        newTask.setMachineName("[\"測試機台\"]");
        newTask.setStartTime(LocalDateTime.now().plusHours(1));
        newTask.setEndTime(LocalDateTime.now().plusHours(2));
        newTask.setTag("電性");
        newTask.setDescription("這是一個單元測試任務");
        newTask.setGroup("114514");
        newTask.setUpdaterId(1);
        newTask.setIsFinish(0);

        Result insertResult = scheduleController.TasksCheckAndAdd(Collections.singletonList(newTask));
        Assertions.assertEquals(1, insertResult.getCode(), "任務新增失敗");
        log.info("新增任務成功");

        // 因為 insertResult.getData() 可能沒有 ID，因此你可以用 DB 或其他手段補上 ID
        // 若 taskService 中會自動填回 ID，那可以直接拿
        // 這裡先手動模擬：假設剛新增任務會自動填上 ID 為 9999
        newTask.setId((Integer) insertResult.getData());

        // === 2. 修改任務 ===
        newTask.setDescription("單元測試更新描述");
        newTask.setUpdaterId(2);  // 模擬不同人修改

        Result updateResult = scheduleController.updateTask(newTask);
        Assertions.assertEquals(1, updateResult.getCode(), "任務修改失敗");
        log.info("修改任務成功");

        // === 3. 刪除任務 ===

        Integer id = (Integer) updateResult.getData();
        newTask.setId(id);
        Result deleteResult = scheduleController.deleteTask(newTask);
        Assertions.assertEquals(1, deleteResult.getCode(), "任務刪除失敗");
        log.info("刪除任務成功，測試結束");
    }
}
