package com.health.system.controller;

import com.health.system.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件上传控制器
 * 用于医学影像图片、报告文件的存储
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件管理", description = "医学影像和报告文件的上传与访问")
public class FileController {

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    @Operation(summary = "上传文件（医学影像/报告）")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "images") String type) {
        if (file.isEmpty()) {
            return Result.badRequest("请选择要上传的文件");
        }

        try {
            // 按日期分目录存储
            String dateDir = LocalDate.now().toString();
            String dirPath = UPLOAD_DIR + File.separator + type + File.separator + dateDir;
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建上传目录失败: {}", dirPath);
                return Result.error("创建目录失败");
            }

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            Path filePath = Paths.get(dirPath, fileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回可访问的URL路径
            String url = "/api/file/" + type + "/" + dateDir + "/" + fileName;
            log.info("文件上传成功: {} -> {}", originalName, url);

            return Result.success("上传成功", url);
        } catch (IOException e) {
            log.error("文件上传失败: ", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 提供文件访问（映射到 uploads 目录）
     * 实际生产环境应使用 Nginx 或 CDN
     */
    @GetMapping("/{type}/{dateDir}/{fileName}")
    @Operation(summary = "获取上传的文件", hidden = true)
    public void getFile(@PathVariable String type,
                        @PathVariable String dateDir,
                        @PathVariable String fileName) {
        // 此方法由静态资源配置处理，实际不走这里
    }
}
