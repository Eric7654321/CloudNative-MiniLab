package com.minilab.controller;

import com.minilab.pojo.entity.Message;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Random;

@SpringBootTest
@Slf4j
public class TestTaskController {

    @Autowired
    private TaskController taskController;

    @Test
    public void testTaskSearch() {
        log.info("測試任務查詢是否正常");
        Result result = taskController.taskSearch("114514");
        log.info("查詢結果: {}", result);
        Assertions.assertEquals(1, result.getCode());
    }

    @Test
    public void testGetTaskToday() {
        log.info("測試今日任務查詢是否正常");
        Result result = taskController.getTaskToday(1); // 用 id=1 測試
        log.info("今日任務查詢結果: {}", result);
        Assertions.assertEquals(1, result.getCode());
    }

    @Test
    public void testGetTaskWeeks() {
        log.info("測試兩週內任務查詢是否正常");
        Result result = taskController.getTaskWeeks(1); // 用 id=1 測試
        log.info("兩週內任務查詢結果: {}", result);
        Assertions.assertEquals(1, result.getCode());
    }

    @Test
    public void testSendMessage() {
        log.info("測試發送訊息是否正常");

        Message message = new Message();
        message.setDescription("這是來自測試的回報訊息");
        message.setStatus(0);
        message.setGroup("114514");

        Result result = taskController.reveiveMsg(message);
        log.info("訊息回報結果: {}", result);
        Assertions.assertEquals(1, result.getCode());
    }

    @Test
    public void testGetMessage() {
        log.info("測試查詢訊息是否正常");

        Result result = taskController.getMsg("114514");
        log.info("訊息查詢結果: {}", result);

        Assertions.assertEquals(1, result.getCode());
        @SuppressWarnings("unchecked")
        List<Message> messages = (List<Message>) result.getData();
        log.info("訊息數量: {}", messages.size());
    }
}
