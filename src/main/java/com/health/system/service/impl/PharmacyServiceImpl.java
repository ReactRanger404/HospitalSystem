package com.health.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.system.common.exception.BusinessException;
import com.health.system.entity.*;
import com.health.system.enums.PrescriptionStatus;
import com.health.system.mapper.*;
import com.health.system.service.MessageProducerService;
import com.health.system.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.health.system.dto.response.DrugVO;
import java.util.ArrayList;
import java.util.List;

/**
 * 药房药库管理服务实现
 *
 * @author health-system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final DrugMapper drugMapper;
    private final DrugInventoryMapper inventoryMapper;
    private final DrugTransactionMapper transactionMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final MessageProducerService messageProducer;

    // ====== 药品管理 ======

    @Override
    @Transactional
    public Drug createDrug(Drug drug) {
        drug.setIsActive(true);
        drugMapper.insert(drug);
        log.info("新增药品: id={}, name={}, code={}", drug.getId(), drug.getName(), drug.getCode());
        return drug;
    }

    @Override
    @Transactional
    public Drug updateDrug(Long id, Drug drug) {
        Drug existing = drugMapper.selectById(id);
        if (existing == null) throw new BusinessException("药品不存在");

        if (drug.getName() != null) existing.setName(drug.getName());
        if (drug.getSpecification() != null) existing.setSpecification(drug.getSpecification());
        if (drug.getSalePrice() != null) existing.setSalePrice(drug.getSalePrice());
        if (drug.getMinStock() != null) existing.setMinStock(drug.getMinStock());
        if (drug.getMaxStock() != null) existing.setMaxStock(drug.getMaxStock());
        if (drug.getIsPrescription() != null) existing.setIsPrescription(drug.getIsPrescription());
        if (drug.getIsActive() != null) existing.setIsActive(drug.getIsActive());
        if (drug.getManufacturer() != null) existing.setManufacturer(drug.getManufacturer());

        drugMapper.updateById(existing);
        log.info("更新药品: id={}", id);
        return existing;
    }

    @Override
    public IPage<DrugVO> getDrugs(String keyword, String category, int page, int size) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<Drug>()
                .orderByAsc(Drug::getCode);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Drug::getName, keyword)
                    .or().like(Drug::getCode, keyword)
                    .or().like(Drug::getGenericName, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Drug::getDrugCategory, category);
        }

        IPage<Drug> drugPage = drugMapper.selectPage(new Page<>(page, size), wrapper);

        // 封装带库存量的结果（使用 DrugVO 确保 JSON 正确序列化）
        List<DrugVO> resultList = new ArrayList<>();
        for (Drug drug : drugPage.getRecords()) {
            // 计算总库存（汇总所有未过期批次的数量）
            Integer totalStock = inventoryMapper.selectList(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, drug.getId())
                            .eq(DrugInventory::getIsActive, true)
                            .ge(DrugInventory::getExpiryDate, LocalDate.now())
            ).stream().map(DrugInventory::getQuantity).reduce(0, Integer::sum);

            resultList.add(DrugVO.builder()
                    .id(drug.getId())
                    .code(drug.getCode())
                    .name(drug.getName())
                    .drugCategory(drug.getDrugCategory())
                    .specification(drug.getSpecification())
                    .unit(drug.getUnit())
                    .manufacturer(drug.getManufacturer())
                    .salePrice(drug.getSalePrice())
                    .minStock(drug.getMinStock())
                    .isPrescription(drug.getIsPrescription())
                    .totalStock(totalStock)
                    .build());
        }

        IPage<DrugVO> resultPage = new Page<>(drugPage.getCurrent(), drugPage.getSize(), drugPage.getTotal());
        resultPage.setRecords(resultList);
        return resultPage;
    }

    // ====== 库存管理 ======

    @Override
    @Transactional
    public DrugInventory receiveDrug(DrugInventory inventory, Long operatorId) {
        inventory.setIsActive(true);
        inventoryMapper.insert(inventory);

        // 记录入库交易
        DrugTransaction transaction = new DrugTransaction();
        transaction.setDrugId(inventory.getDrugId());
        transaction.setInventoryId(inventory.getId());
        transaction.setTransactionType("purchase");
        transaction.setQuantity(inventory.getQuantity());
        transaction.setUnitPrice(inventory.getPurchasePrice());
        transaction.setTotalPrice(inventory.getPurchasePrice().multiply(BigDecimal.valueOf(inventory.getQuantity())));
        transaction.setOperatorId(operatorId);
        transaction.setReferenceType("purchase_order");
        transaction.setTransactionDate(java.time.LocalDateTime.now());
        transactionMapper.insert(transaction);

        log.info("药品入库: drugId={}, quantity={}, batch={}, operatorId={}",
                inventory.getDrugId(), inventory.getQuantity(), inventory.getBatchNumber(), operatorId);
        return inventory;
    }

    @Override
    public IPage<DrugInventory> getInventory(Long drugId, Boolean nearExpiry, int page, int size) {
        LambdaQueryWrapper<DrugInventory> wrapper = new LambdaQueryWrapper<DrugInventory>()
                .eq(DrugInventory::getIsActive, true)
                .orderByAsc(DrugInventory::getExpiryDate);
        if (drugId != null) wrapper.eq(DrugInventory::getDrugId, drugId);
        if (Boolean.TRUE.equals(nearExpiry)) {
            wrapper.le(DrugInventory::getExpiryDate, LocalDate.now().plusMonths(3))
                    .ge(DrugInventory::getExpiryDate, LocalDate.now());
        }
        return inventoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<DrugInventory> getExpiryWarnings() {
        return inventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .eq(DrugInventory::getIsActive, true)
                        .le(DrugInventory::getExpiryDate, LocalDate.now().plusMonths(3))
                        .ge(DrugInventory::getExpiryDate, LocalDate.now())
                        .gt(DrugInventory::getQuantity, 0)
                        .orderByAsc(DrugInventory::getExpiryDate));
    }

    // ====== 配药发药 ======

    @Override
    @Transactional
    public Prescription dispensePrescription(Long prescriptionId, Long pharmacistId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) throw new BusinessException("处方不存在");
        if (prescription.getStatus() != PrescriptionStatus.PAID) {
            throw new BusinessException("处方状态不允许发药: " + prescription.getStatus().getLabel());
        }

        // 获取处方明细
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));

        // 扣减库存（FIFO先进先出）
        for (PrescriptionItem item : items) {
            if ("rejected".equals(item.getAuditStatus())) continue;

            int toDispense = item.getQuantity();
            List<DrugInventory> inventories = inventoryMapper.selectList(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .eq(DrugInventory::getIsActive, true)
                            .gt(DrugInventory::getQuantity, 0)
                            .ge(DrugInventory::getExpiryDate, LocalDate.now())
                            .orderByAsc(DrugInventory::getExpiryDate));

            for (DrugInventory inv : inventories) {
                if (toDispense <= 0) break;
                int deduct = Math.min(toDispense, inv.getQuantity());
                inv.setQuantity(inv.getQuantity() - deduct);
                inventoryMapper.updateById(inv);
                toDispense -= deduct;

                // 记录出库交易
                DrugTransaction transaction = new DrugTransaction();
                transaction.setDrugId(item.getDrugId());
                transaction.setInventoryId(inv.getId());
                transaction.setTransactionType("dispense");
                transaction.setQuantity(-deduct);
                transaction.setUnitPrice(item.getUnitPrice());
                transaction.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(deduct)));
                transaction.setOperatorId(pharmacistId);
                transaction.setReferenceType("prescription");
                transaction.setReferenceId(prescriptionId);
                transaction.setTransactionDate(java.time.LocalDateTime.now());
                transactionMapper.insert(transaction);
            }

            if (toDispense > 0) {
                log.warn("药品库存不足: drugId={}, need={}, lack={}", item.getDrugId(), item.getQuantity(), toDispense);
            }
        }

        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescriptionMapper.updateById(prescription);

        log.info("发药完成: prescriptionId={}, pharmacistId={}", prescriptionId, pharmacistId);
        return prescription;
    }

    @Override
    public IPage<Prescription> getPendingDispense(int page, int size) {
        return prescriptionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, PrescriptionStatus.PAID)
                        .orderByDesc(Prescription::getCreatedAt));
    }

    // ====== 处方前置审核 ======

    @Override
    @Transactional
    public PrescriptionItem auditItem(Long itemId, String status, String note) {
        PrescriptionItem item = prescriptionItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException("处方明细不存在");

        item.setAuditStatus(status);
        item.setAuditNote(note);
        prescriptionItemMapper.updateById(item);

        log.info("处方前置审核: itemId={}, status={}, note={}", itemId, status, note);

        // 发送通知给开单医生
        Prescription prescription = prescriptionMapper.selectById(item.getPrescriptionId());
        if (prescription != null) {
            messageProducer.sendPrescriptionAuditNotification(
                    prescription.getId(), prescription.getDoctorId(), status, note
            );
        }

        return item;
    }

    // ====== 交易记录 ======

    @Override
    public IPage<DrugTransaction> getTransactions(Long drugId, String type, int page, int size) {
        LambdaQueryWrapper<DrugTransaction> wrapper = new LambdaQueryWrapper<DrugTransaction>()
                .orderByDesc(DrugTransaction::getTransactionDate);
        if (drugId != null) wrapper.eq(DrugTransaction::getDrugId, drugId);
        if (type != null) wrapper.eq(DrugTransaction::getTransactionType, type);
        return transactionMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
