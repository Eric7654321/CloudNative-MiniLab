package com.minilab.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {
    Integer id;
    Integer emp;
    String empName;
    Integer machine;
    String machineName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String description;
    Integer isFinish;
    LocalDateTime updateTime;
}
