package com.health.system.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.system.common.exception.BusinessException;
import com.health.system.entity.*;
import com.health.system.enums.PrescriptionStatus;
import com.health.system.mapper.*;
import com.health.system.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 收费与财务结算服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final PaymentMapper paymentMapper;
    private final InpatientAccountMapper inpatientAccountMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final UserMapper userMapper;

    /** 生成账单编号 */
    private String generateBillNo(String type) {
        String prefix = switch (type) {
            case "registration" -> "REG";
            case "prescription" -> "PRE";
            case "examination" -> "EXM";
            case "hospitalization" -> "HOS";
            default -> "BIL";
        };
        String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        return prefix + dateStr;
    }

    private String generatePaymentNo() {
        return "PAY" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
    }

    private String generateAdmissionNo() {
        return "INP" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
    }

    // ====== 门诊收费 ======

    @Override
    @Transactional
    public Bill createBill(Bill bill, List<BillItem> items) {
        BigDecimal total = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setBillNo(generateBillNo(bill.getBillType()));
        bill.setTotalAmount(total);
        bill.setPayableAmount(total);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setStatus("pending");
        billMapper.insert(bill);

        for (BillItem item : items) {
            item.setBillId(bill.getId());
            billItemMapper.insert(item);
        }

        log.info("创建账单: id={}, no={}, type={}, amount={}, patientId={}",
                bill.getId(), bill.getBillNo(), bill.getBillType(), total, bill.getPatientId());
        return bill;
    }

    @Override
    @Transactional
    public Payment processPayment(Long billId, BigDecimal amount, String method, Long operatorId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) throw new BusinessException("账单不存在");
        if ("paid".equals(bill.getStatus())) throw new BusinessException("账单已支付");
        if ("cancelled".equals(bill.getStatus())) throw new BusinessException("账单已取消");

        // 创建支付记录
        Payment payment = new Payment();
        payment.setBillId(billId);
        payment.setPaymentNo(generatePaymentNo());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentType("full");
        payment.setOperatorId(operatorId);
        payment.setStatus("success");
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        // 更新账单状态
        bill.setPaidAmount(bill.getPaidAmount().add(amount));
        if (bill.getPaidAmount().compareTo(bill.getPayableAmount()) >= 0) {
            bill.setStatus("paid");
            bill.setPaidAt(LocalDateTime.now());

            // 关联处方更新为已缴费
            if ("prescription".equals(bill.getReferenceType()) && bill.getReferenceId() != null) {
                Prescription prescription = prescriptionMapper.selectById(bill.getReferenceId());
                if (prescription != null && prescription.getStatus() == PrescriptionStatus.PENDING) {
                    prescription.setStatus(PrescriptionStatus.PAID);
                    prescriptionMapper.updateById(prescription);
                }
            }
        } else {
            bill.setStatus("partially_paid");
        }
        billMapper.updateById(bill);

        log.info("支付成功: paymentNo={}, billId={}, amount={}, method={}",
                payment.getPaymentNo(), billId, amount, method);
        return payment;
    }

    @Override
    @Transactional
    public Bill refundBill(Long billId, Long operatorId, String reason) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) throw new BusinessException("账单不存在");
        if (!"paid".equals(bill.getStatus())) throw new BusinessException("账单未支付，不能退费");

        // 创建退款记录
        Payment refund = new Payment();
        refund.setBillId(billId);
        refund.setPaymentNo(generatePaymentNo());
        refund.setAmount(bill.getPaidAmount().negate());
        refund.setPaymentMethod("refund");
        refund.setPaymentType("refund");
        refund.setOperatorId(operatorId);
        refund.setNote(reason);
        refund.setStatus("refunded");
        refund.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(refund);

        bill.setStatus("refunded");
        bill.setPaidAmount(BigDecimal.ZERO);
        billMapper.updateById(bill);

        // 关联处方更新为待缴费
        if ("prescription".equals(bill.getReferenceType()) && bill.getReferenceId() != null) {
            Prescription prescription = prescriptionMapper.selectById(bill.getReferenceId());
            if (prescription != null && prescription.getStatus() == PrescriptionStatus.PAID) {
                prescription.setStatus(PrescriptionStatus.PENDING);
                prescriptionMapper.updateById(prescription);
            }
        }

        log.info("退费处理: billId={}, amount={}, operatorId={}, reason={}",
                billId, bill.getPaidAmount(), operatorId, reason);
        return bill;
    }

    @Override
    public IPage<Bill> getBills(Long patientId, String billType, String status, int page, int size) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<Bill>()
                .orderByDesc(Bill::getCreatedAt);
        if (patientId != null) wrapper.eq(Bill::getPatientId, patientId);
        if (billType != null) wrapper.eq(Bill::getBillType, billType);
        if (status != null) wrapper.eq(Bill::getStatus, status);
        return billMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ====== 住院结账 ======

    @Override
    @Transactional
    public InpatientAccount createInpatientAccount(InpatientAccount account) {
        account.setAdmissionNumber(generateAdmissionNo());
        account.setAdmissionDate(LocalDateTime.now());
        account.setDepositAmount(account.getDepositAmount() != null ? account.getDepositAmount() : BigDecimal.ZERO);
        account.setTotalExpenses(BigDecimal.ZERO);
        account.setBalance(account.getDepositAmount().negate()); // 负数为预缴
        account.setStatus("admitted");
        inpatientAccountMapper.insert(account);

        log.info("入院登记: id={}, admissionNo={}, patientId={}",
                account.getId(), account.getAdmissionNumber(), account.getPatientId());
        return account;
    }

    @Override
    @Transactional
    public InpatientAccount addDeposit(Long accountId, BigDecimal amount, String method) {
        InpatientAccount account = inpatientAccountMapper.selectById(accountId);
        if (account == null) throw new BusinessException("住院账户不存在");
        if (!"admitted".equals(account.getStatus())) throw new BusinessException("非住院状态，不能缴纳押金");

        account.setDepositAmount(account.getDepositAmount().add(amount));
        account.setBalance(account.getBalance().subtract(amount)); // 余额减少（更多预缴）
        inpatientAccountMapper.updateById(account);

        log.info("住院押金缴纳: accountId={}, amount={}, method={}", accountId, amount, method);
        return account;
    }

    @Override
    @Transactional
    public InpatientAccount dischargeSettle(Long accountId) {
        InpatientAccount account = inpatientAccountMapper.selectById(accountId);
        if (account == null) throw new BusinessException("住院账户不存在");
        if (!"admitted".equals(account.getStatus())) throw new BusinessException("该患者已出院");

        account.setStatus("discharged");
        account.setDischargeDate(LocalDateTime.now());
        inpatientAccountMapper.updateById(account);

        log.info("出院结算: accountId={}, totalExpenses={}, balance={}",
                accountId, account.getTotalExpenses(), account.getBalance());
        return account;
    }

    // ====== 财务报表 ======

    @Override
    public Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        // 统计日期范围内已支付的账单
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalBills = 0;

        // 按日统计
        List<Bill> paidBills = billMapper.selectList(
                new LambdaQueryWrapper<Bill>()
                        .eq(Bill::getStatus, "paid")
                        .ge(Bill::getPaidAt, startDate.atStartOfDay())
                        .le(Bill::getPaidAt, endDate.plusDays(1).atStartOfDay()));

        Map<String, BigDecimal> dailyMap = new LinkedHashMap<>();
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Bill bill : paidBills) {
            String day = bill.getPaidAt().toLocalDate().toString();
            dailyMap.merge(day, bill.getPayableAmount(), BigDecimal::add);
            countMap.merge(day, 1L, Long::sum);
        }

        for (Map.Entry<String, BigDecimal> entry : dailyMap.entrySet()) {
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", entry.getKey());
            dayStat.put("revenue", entry.getValue());
            dayStat.put("count", countMap.getOrDefault(entry.getKey(), 0L));
            dailyBreakdown.add(dayStat);
            totalRevenue = totalRevenue.add(entry.getValue());
            totalBills += countMap.getOrDefault(entry.getKey(), 0L);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalRevenue", totalRevenue);
        result.put("totalBills", totalBills);
        result.put("dailyBreakdown", dailyBreakdown);
        return result;
    }

    @Override
    public List<Map<String, Object>> getDepartmentRevenue(LocalDate startDate, LocalDate endDate) {
        // 按账单类型统计
        List<Bill> paidBills = billMapper.selectList(
                new LambdaQueryWrapper<Bill>()
                        .eq(Bill::getStatus, "paid")
                        .ge(Bill::getPaidAt, startDate.atStartOfDay())
                        .le(Bill::getPaidAt, endDate.plusDays(1).atStartOfDay()));

        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (Bill bill : paidBills) {
            String type = bill.getBillType();
            revenueMap.merge(type, bill.getPayableAmount(), BigDecimal::add);
            countMap.merge(type, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());
            item.put("revenue", entry.getValue());
            item.put("count", countMap.get(entry.getKey()));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getDoctorWorkload(LocalDate startDate, LocalDate endDate) {
        // 医生工作量：就诊人数 + 处方金额
        List<Map<String, Object>> result = new ArrayList<>();

        List<User> doctors = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "doctor"));
        if (doctors.isEmpty()) return result;

        for (User doctor : doctors) {
            // 就诊人数
            Long recordCount = medicalRecordMapper.selectCount(
                    new LambdaQueryWrapper<MedicalRecord>()
                            .eq(MedicalRecord::getDoctorId, doctor.getId())
                            .ge(MedicalRecord::getVisitDate, startDate)
                            .le(MedicalRecord::getVisitDate, endDate));

            // 处方金额
            List<Prescription> prescriptions = prescriptionMapper.selectList(
                    new LambdaQueryWrapper<Prescription>()
                            .eq(Prescription::getDoctorId, doctor.getId())
                            .eq(Prescription::getStatus, PrescriptionStatus.DISPENSED)
                            .ge(Prescription::getCreatedAt, startDate.atStartOfDay())
                            .le(Prescription::getCreatedAt, endDate.plusDays(1).atStartOfDay()));

            BigDecimal totalAmount = prescriptions.stream()
                    .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("doctorId", doctor.getId());
            item.put("doctorName", doctor.getRealName());
            item.put("departmentId", doctor.getDepartmentId());
            item.put("recordCount", recordCount);
            item.put("prescriptionCount", prescriptions.size());
            item.put("prescriptionAmount", totalAmount);
            result.add(item);
        }
        return result;
    }
}
