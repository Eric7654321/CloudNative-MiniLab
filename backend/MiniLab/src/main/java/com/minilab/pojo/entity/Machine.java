package com.minilab.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Machine {
    Integer id;
    String name;
    String machineName;
    Integer usable;
    String group;
    LocalDateTime updateTime;
}
