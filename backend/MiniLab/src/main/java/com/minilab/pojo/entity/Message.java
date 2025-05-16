package com.minilab.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    Integer id;
    Integer taskId;
    String description;
    String group;
    Integer status;
    LocalDateTime updateTime;
}
