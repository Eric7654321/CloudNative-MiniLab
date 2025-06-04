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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        newTask.setMachine("[\"999\"]");
        newTask.setMachineName("[\"測試機台\"]");
        newTask.setStartTime(LocalDateTime.of(2099, 1, 1, 10, 0));
        newTask.setEndTime(LocalDateTime.of(2099, 1, 1, 12, 0));
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

    @Test
    public void testTaskFailedByOverlapping() {
        log.info("測試新增任務時，若時間衝突應失敗");

        // 建立第二個任務 B（與 A 完全重疊）
        Task taskB = new Task();
        taskB.setEmp(1);
        taskB.setMachine("[\"1\",\"43\"]");
        taskB.setStartTime(LocalDateTime.now().plusHours(1));
        taskB.setEndTime(LocalDateTime.now().plusHours(6));
        taskB.setTag("電性");
        taskB.setDescription("應該會衝突吧");
        taskB.setGroup("114514");
        taskB.setUpdaterId(1);
        taskB.setIsFinish(0);

        Result resultB = scheduleController.TasksCheckAndAdd(Collections.singletonList(taskB));
        Assertions.assertNotEquals(1, resultB.getCode(), "重疊任務竟然被新增成功！");
        log.info("第二個任務新增失敗，錯誤訊息：" + resultB.getMsg());
    }

    @Test
    public void testTaskFailedByTooWeak() {
        log.info("測試新增任務時，若能力不符應失敗");

        Task taskB = new Task();
        taskB.setEmp(1);
        taskB.setMachine("[\"1\",\"43\"]");
        taskB.setStartTime(LocalDateTime.now().plusHours(100));
        taskB.setEndTime(LocalDateTime.now().plusHours(150));
        taskB.setTag("溫度");
        taskB.setDescription("應該會衝突");
        taskB.setGroup("114514");
        taskB.setUpdaterId(1);

        Result resultB = scheduleController.TasksCheckAndAdd(Collections.singletonList(taskB));
        Assertions.assertNotEquals(1, resultB.getCode(), "重疊任務竟然被新增成功！");
        log.info("第二個任務新增失敗，錯誤訊息：" + resultB.getMsg());
    }

    @Test
    public void testTaskFailedEmpIsScheduled() {
        log.info("測試新增任務時，若工人與已存在任務時間重疊應失敗");

        Task task = new Task();
        task.setEmp(1); // 與資料庫中那筆 emp 相同
        task.setMachine("[\"999\"]"); // 換一台機器以避免機器衝突
        task.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0)); // 落在已存在任務區間
        task.setEndTime(LocalDateTime.of(2026, 1, 1, 12, 0));
        task.setTag("電性");
        task.setDescription("工人衝突測試");
        task.setGroup("114514");
        task.setUpdaterId(1);
        task.setIsFinish(0);

        Result result = scheduleController.TasksCheckAndAdd(Collections.singletonList(task));
        Assertions.assertNotEquals(1, result.getCode(), "工人衝突的任務竟然被新增成功！");
        log.info("任務新增失敗，錯誤訊息：" + result.getMsg());
    }

    @Test
    public void testTaskFailedMachineIsScheduled() {
        log.info("測試新增任務時，若機器與已存在任務時間重疊應失敗");

        Task task = new Task();
        task.setEmp(2); // 換一個員工，避免人員衝突
        task.setMachine("[\"1\"]"); // 與資料庫中那筆相同
        task.setStartTime(LocalDateTime.of(2026, 1, 1, 10, 0)); // 落在已存在任務區間
        task.setEndTime(LocalDateTime.of(2026, 1, 1, 12, 0));
        task.setTag("電性");
        task.setDescription("機器衝突測試");
        task.setGroup("114514");
        task.setUpdaterId(2);
        task.setIsFinish(0);

        Result result = scheduleController.TasksCheckAndAdd(Collections.singletonList(task));
        Assertions.assertNotEquals(1, result.getCode(), "機器衝突的任務竟然被新增成功！");
        log.info("任務新增失敗，錯誤訊息：" + result.getMsg());
    }
}
