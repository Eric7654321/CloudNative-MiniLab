package com.minilab.service.impl;

import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Task;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    public List<Task> getTaskByIdAndTime(Integer id, LocalDate now) {
        List<Task> tasks = taskMapper.getTaskByIdAndTime(id, now, now);
        log.info("已查詢到tasks: {}, id= {}, day= {}", tasks, id);
        return List.of();
    }

    @Override
    public List<Task> getTaskByIdAndTimeWeeks(Integer id, LocalDate now) {
        List<Task> tasks = taskMapper.getTaskByIdAndTime(id, now, now.plusDays(14));
        log.info("已查詢到兩個禮拜內tasks: {}, id= {}, day= {}", tasks, id);
        return List.of();
    }

    @Override
    public void deleteTask(Task task) {
        taskMapper.deleteTaskById(task.getId());
    }

    @Override
    public void updateTask(Task task) {
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateTask(task);
    }
}
