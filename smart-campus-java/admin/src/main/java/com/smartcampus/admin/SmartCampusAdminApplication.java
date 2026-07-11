package com.smartcampus.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.smartcampus.common", "com.smartcampus.admin"})
@MapperScan(basePackages = {"com.smartcampus.common.mapper"})
public class SmartCampusAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusAdminApplication.class, args);
    }
}
