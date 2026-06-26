package com.health.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 收费与财务结算服务接口
 * 门诊收费、住院结账、财务报表统计
 *
 * @author health-system
 */
public interface BillingService {

    // ====== 门诊收费 ======
    Bill createBill(Bill bill, List<BillItem> items);
    Payment processPayment(Long billId, BigDecimal amount, String method, Long operatorId);
    Bill refundBill(Long billId, Long operatorId, String reason);
    IPage<Bill> getBills(Long patientId, String billType, String status, int page, int size);

    // ====== 住院结账 ======
    InpatientAccount createInpatientAccount(InpatientAccount account);
    InpatientAccount addDeposit(Long accountId, BigDecimal amount, String method);
    InpatientAccount dischargeSettle(Long accountId);

    // ====== 财务报表 ======
    Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getDepartmentRevenue(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getDoctorWorkload(LocalDate startDate, LocalDate endDate);
}
