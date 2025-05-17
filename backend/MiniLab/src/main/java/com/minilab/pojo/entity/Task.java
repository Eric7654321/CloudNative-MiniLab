package com.minilab.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    Integer id;
    Integer emp; //無須在意session lock
    String empName; //根據empid來修改
    String machine; //無須在意session lock
    String machineName; //根據empid來修改
    LocalDateTime startTime; //無須在意session lock
    LocalDateTime endTime; //無須在意session lock
    String tag;
    String description; //無須在意session lock
    String group;
    Integer updaterId; //根據修改者來修改
    Integer isFinish; //無須在意session lock
    LocalDateTime updateTime; //無須在意session lock
}
