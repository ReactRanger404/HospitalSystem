package com.health.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置
 * 使上传的文件可通过 URL 直接访问
 *
 * @author health-system
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射文件上传目录
        registry.addResourceHandler("/api/file/**")
                .addResourceLocations("file:uploads/");
    }
}
