package com.minilab.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {
    Integer id;
    String name;
    String machineName;
    Integer usable;
    String group;
    LocalDateTime updateTime;
}
