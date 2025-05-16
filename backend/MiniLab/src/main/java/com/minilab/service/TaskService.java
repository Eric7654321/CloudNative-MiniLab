package com.minilab.service;

import com.minilab.pojo.entity.Task;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {
    void deleteTask(Task task);

    void updateTask(Task task);

    List<Task> getTaskByGroupId(String groupId);

    List<Task> getTaskByIdAndTimeWeeks(Integer id, LocalDate now);

    List<Task> getTaskByIdAndTime(Integer id, LocalDate now);
}
