package com.health.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/Swagger API 文档配置
 * 访问地址: http://localhost:8080/api/doc.html
 *
 * @author health-system
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医院综合信息管理系统 API")
                        .version("1.0.0")
                        .description("门诊挂号预约、医生站、医技工作站、药房药库、收费财务等模块的 RESTful API")
                        .contact(new Contact()
                                .name("Health System Team")
                                .email("admin@hospital.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
