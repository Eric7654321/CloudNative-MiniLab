package com.minilab;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
//@MapperScan("com.minilab.mapper")
public class MiniLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniLabApplication.class, args);
    }

}
