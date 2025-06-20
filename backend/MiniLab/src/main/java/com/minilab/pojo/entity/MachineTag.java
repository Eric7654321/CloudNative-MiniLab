package com.minilab.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineTag {
    Integer id;
    Integer machineId;
    String tags;
    LocalDateTime updateTime;
}
