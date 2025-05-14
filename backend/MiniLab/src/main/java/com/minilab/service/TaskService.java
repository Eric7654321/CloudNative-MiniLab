package com.minilab.service;

import com.minilab.pojo.entity.Task;

import java.util.List;

public interface TaskService {
    List<Task> getTaskByGroupId(String groupId);
}
