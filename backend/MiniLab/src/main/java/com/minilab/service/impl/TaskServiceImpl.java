package com.minilab.service.impl;

import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Task;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    TaskMapper taskMapper;

    @Override
    public List<Task> getTaskByGroupId(String groupId) {
        List<Task> tasks = taskMapper.getTaskByGroupId(groupId);
        log.info("已查詢到tasks: {}, groupId={}", tasks, groupId);
        return tasks;
    }
}
