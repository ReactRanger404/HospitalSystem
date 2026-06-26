package com.health.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.common.result.PageResult;
import com.health.system.common.result.Result;
import com.health.system.dto.response.DrugVO;
import com.health.system.entity.*;
import com.health.system.service.PharmacyService;
import com.health.system.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 药房药库管理控制器
 * 药品管理、库存管理、配药发药、处方前置审核
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
@Tag(name = "药房药库管理", description = "药品信息管理、入库出库、配药发药、处方审核、库存预警")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    // ====== 药品管理（仅药剂师和管理员可修改） ======
    @PostMapping("/drugs")
    @PreAuthorize("hasAnyRole('pharmacist', 'admin')")
    @Operation(summary = "新增药品")
    public Result<Drug> createDrug(@RequestBody Drug drug) {
        return Result.success(pharmacyService.createDrug(drug));
    }

    @PutMapping("/drugs/{id}")
    @PreAuthorize("hasAnyRole('pharmacist', 'admin')")
    @Operation(summary = "更新药品信息")
    public Result<Drug> updateDrug(@PathVariable Long id, @RequestBody Drug drug) {
        return Result.success(pharmacyService.updateDrug(id, drug));
    }

    @GetMapping("/drugs")
    @Operation(summary = "查询药品列表")
    public Result<PageResult<DrugVO>> getDrugs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<DrugVO> result = pharmacyService.getDrugs(keyword, category, page, size);
        return Result.success(PageResult.of(result));
    }

    // ====== 库存管理 ======
    @PostMapping("/inventory/receive")
    @Operation(summary = "药品入库")
    public Result<DrugInventory> receiveDrug(@RequestBody DrugInventory inventory,
                                             @RequestParam Long operatorId) {
        return Result.success(pharmacyService.receiveDrug(inventory, operatorId));
    }

    @GetMapping("/inventory")
    @Operation(summary = "查询库存列表")
    public Result<PageResult<DrugInventory>> getInventory(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Boolean nearExpiry,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<DrugInventory> result = pharmacyService.getInventory(drugId, nearExpiry, page, size);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/inventory/expiry-warnings")
    @Operation(summary = "临期药品预警")
    public Result<List<DrugInventory>> getExpiryWarnings() {
        return Result.success(pharmacyService.getExpiryWarnings());
    }

    // ====== 配药发药 ======
    @GetMapping("/dispense/pending")
    @Operation(summary = "获取待发药处方列表")
    public Result<PageResult<Prescription>> getPendingDispense(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<Prescription> result = pharmacyService.getPendingDispense(page, size);
        return Result.success(PageResult.of(result));
    }

    @PutMapping("/dispense/{prescriptionId}")
    @Operation(summary = "配药发药")
    public Result<Prescription> dispensePrescription(@PathVariable Long prescriptionId,
                                                     @RequestParam Long pharmacistId) {
        return Result.success(pharmacyService.dispensePrescription(prescriptionId, pharmacistId));
    }

    // ====== 处方前置审核 ======
    @PutMapping("/audit/{itemId}")
    @Operation(summary = "处方前置审核（单条药品）")
    public Result<PrescriptionItem> auditItem(@PathVariable Long itemId,
                                              @RequestParam String status,
                                              @RequestParam(required = false) String note) {
        return Result.success(pharmacyService.auditItem(itemId, status, note));
    }

    // ====== 交易记录 ======
    @GetMapping("/transactions")
    @Operation(summary = "查询出入库记录")
    public Result<PageResult<DrugTransaction>> getTransactions(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<DrugTransaction> result = pharmacyService.getTransactions(drugId, type, page, size);
        return Result.success(PageResult.of(result));
    }
}
