package com.minilab.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpVO {
    Integer id;
    String username;
    String name;
    Integer usable;
    String group;
    LocalDateTime updateTime;
    Integer role;
    String jwt;
}
