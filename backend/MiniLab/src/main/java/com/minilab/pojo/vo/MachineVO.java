package com.minilab.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineVO {
    Integer id;
    String name;
    String machineName;
    Integer usable;
    String group;
    LocalDateTime updateTime;
    String tags;
}
