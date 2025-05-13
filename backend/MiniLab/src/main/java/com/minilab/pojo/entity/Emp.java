package com.minilab.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emp {
    Integer id;
    String username;
    String name;
    String password;
    Integer usable;
    String group;
    LocalDateTime updateTime;
    Integer role;
}
