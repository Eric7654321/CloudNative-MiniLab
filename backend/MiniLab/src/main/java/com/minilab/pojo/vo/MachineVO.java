package com.minilab.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MachineVO {
    Integer id;
    String name;
    String machineName;
    Integer usable;
    String group;
    LocalDateTime updateTime;
    String tags;
}
