package com.health.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.common.result.PageResult;
import com.health.system.common.result.Result;
import com.health.system.entity.*;
import com.health.system.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 收费与财务结算控制器
 * 门诊收费、住院结账、财务报表
 *
 * @author health-system
 */
@Slf4j
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Tag(name = "收费与财务结算", description = "门诊收费、住院押金与结算、财务报表统计")
public class BillingController {

    private final BillingService billingService;

    // ====== 门诊收费 ======
    @PostMapping("/bills")
    @Operation(summary = "创建账单")
    public Result<Bill> createBill(@RequestBody Map<String, Object> params) {
        Bill bill = new Bill();
        bill.setPatientId(Long.valueOf(params.get("patientId").toString()));
        bill.setBillType((String) params.get("billType"));
        if (params.get("referenceId") != null)
            bill.setReferenceId(Long.valueOf(params.get("referenceId").toString()));
        if (params.get("referenceType") != null)
            bill.setReferenceType((String) params.get("referenceType"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) params.get("items");
        List<BillItem> items = itemsData.stream().map(m -> {
            BillItem item = new BillItem();
            item.setItemType((String) m.get("itemType"));
            item.setItemName((String) m.get("itemName"));
            item.setQuantity(m.get("quantity") != null ? Integer.valueOf(m.get("quantity").toString()) : 1);
            item.setUnitPrice(m.get("unitPrice") != null ? new BigDecimal(m.get("unitPrice").toString()) : BigDecimal.ZERO);
            item.setAmount(m.get("amount") != null ? new BigDecimal(m.get("amount").toString()) : BigDecimal.ZERO);
            return item;
        }).toList();

        return Result.success(billingService.createBill(bill, items));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('admin', 'nurse', 'pharmacist')")
    @Operation(summary = "支付处理（微信/支付宝/医保/现金）")
    public Result<Payment> processPayment(@RequestParam Long billId,
                                          @RequestParam BigDecimal amount,
                                          @RequestParam String method,
                                          @RequestParam(required = false) Long operatorId) {
        return Result.success(billingService.processPayment(billId, amount, method, operatorId));
    }

    @PutMapping("/bills/{id}/refund")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "退费处理")
    public Result<Bill> refundBill(@PathVariable Long id,
                                   @RequestParam Long operatorId,
                                   @RequestParam(required = false) String reason) {
        return Result.success(billingService.refundBill(id, operatorId, reason));
    }

    @GetMapping("/bills")
    @Operation(summary = "查询账单列表")
    public Result<PageResult<Bill>> getBills(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String billType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<Bill> result = billingService.getBills(patientId, billType, status, page, size);
        return Result.success(PageResult.of(result));
    }

    // ====== 住院结账 ======
    @PostMapping("/inpatient")
    @Operation(summary = "入院登记")
    public Result<InpatientAccount> createInpatient(@RequestBody InpatientAccount account) {
        return Result.success(billingService.createInpatientAccount(account));
    }

    @PutMapping("/inpatient/{id}/deposit")
    @Operation(summary = "缴纳住院押金")
    public Result<InpatientAccount> addDeposit(@PathVariable Long id,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam String method) {
        return Result.success(billingService.addDeposit(id, amount, method));
    }

    @PutMapping("/inpatient/{id}/discharge")
    @Operation(summary = "出院结算")
    public Result<InpatientAccount> dischargeSettle(@PathVariable Long id) {
        return Result.success(billingService.dischargeSettle(id));
    }

    // ====== 财务报表 ======
    @GetMapping("/reports/revenue")
    @Operation(summary = "收入统计报表")
    public Result<Map<String, Object>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(billingService.getRevenueReport(startDate, endDate));
    }

    @GetMapping("/reports/department")
    @Operation(summary = "科室收入统计")
    public Result<List<Map<String, Object>>> getDepartmentRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(billingService.getDepartmentRevenue(startDate, endDate));
    }

    @GetMapping("/reports/doctor-workload")
    @Operation(summary = "医生工作量统计")
    public Result<List<Map<String, Object>>> getDoctorWorkload(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(billingService.getDoctorWorkload(startDate, endDate));
    }
}
