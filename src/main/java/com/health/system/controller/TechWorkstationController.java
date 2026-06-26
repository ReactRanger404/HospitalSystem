package com.health.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.common.result.PageResult;
import com.health.system.common.result.Result;
import com.health.system.entity.ExamRequest;
import com.health.system.entity.ExamResult;
import com.health.system.service.TechWorkstationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医技工作站控制器
 * 检查检验接诊、结果录入、审核发布
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/tech")
@RequiredArgsConstructor
@Tag(name = "医技工作站", description = "检查检验接诊登记、报告录入与审核发布")
public class TechWorkstationController {

    private final TechWorkstationService techService;

    @GetMapping("/pending")
    @Operation(summary = "获取待接诊的检查检验申请")
    public Result<PageResult<ExamRequest>> getPendingExams(
            @RequestParam(required = false) String examCategory,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<ExamRequest> result = techService.getPendingExams(examCategory, page, size);
        return Result.success(PageResult.of(result));
    }

    @PutMapping("/exams/{id}/start")
    @Operation(summary = "登记接诊")
    public Result<ExamRequest> startExam(@PathVariable Long id,
                                         @RequestParam Long technicianId) {
        return Result.success(techService.startExam(id, technicianId));
    }

    @PostMapping("/results")
    @Operation(summary = "录入/保存检查检验结果")
    public Result<ExamResult> saveResult(@RequestParam Long examRequestId,
                                         @RequestParam Long technicianId,
                                         @RequestBody ExamResult result) {
        return Result.success(techService.saveResult(examRequestId, technicianId, result));
    }

    @PutMapping("/results/{id}/submit")
    @Operation(summary = "提交报告审核")
    public Result<ExamResult> submitForReview(@PathVariable Long id) {
        return Result.success(techService.submitForReview(id));
    }

    @PutMapping("/results/{id}/review")
    @Operation(summary = "审核报告")
    public Result<ExamResult> reviewResult(@PathVariable Long id,
                                           @RequestParam Long reviewerId,
                                           @RequestParam String decision,
                                           @RequestParam(required = false) String comment) {
        return Result.success(techService.reviewResult(id, reviewerId, decision, comment));
    }

    @PutMapping("/results/{id}/publish")
    @Operation(summary = "发布报告（同步至医生/患者端）")
    public Result<ExamResult> publishResult(@PathVariable Long id) {
        return Result.success(techService.publishResult(id));
    }

    @GetMapping("/results")
    @Operation(summary = "查询检查检验结果列表")
    public Result<PageResult<ExamResult>> getResults(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<ExamResult> result = techService.getResults(status, page, size);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/patients/{patientId}/results")
    @Operation(summary = "查询患者的已发布检查结果")
    public Result<List<ExamResult>> getPatientResults(@PathVariable Long patientId) {
        return Result.success(techService.getPatientResults(patientId));
    }
}
