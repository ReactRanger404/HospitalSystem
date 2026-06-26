package com.health.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 医院信息管理系统 - 启动类
 * 技术栈: Spring Boot 3.2 + MyBatis-Plus + Redis + Kafka + MySQL
 *
 * @author health-system
 */
@SpringBootApplication
@MapperScan("com.health.system.mapper")
@EnableCaching      // 启用缓存（Redis）
@EnableScheduling   // 启用定时任务
public class HospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  医院综合信息管理系统启动成功！");
        System.out.println("  Swagger文档: http://localhost:8080/api/doc.html");
        System.out.println("===========================================");
    }
}
