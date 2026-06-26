package com.health.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.health.system.dto.response.DrugVO;
import com.health.system.entity.*;

import java.util.List;

/**
 * 药房药库管理服务接口
 * 药品进销存、配药发药、处方前置审核
 *
 * @author health-system
 */
public interface PharmacyService {

    // ====== 药品管理 ======
    Drug createDrug(Drug drug);
    Drug updateDrug(Long id, Drug drug);
    IPage<DrugVO> getDrugs(String keyword, String category, int page, int size);

    // ====== 库存管理 ======
    DrugInventory receiveDrug(DrugInventory inventory, Long operatorId);
    IPage<DrugInventory> getInventory(Long drugId, Boolean nearExpiry, int page, int size);
    List<DrugInventory> getExpiryWarnings();

    // ====== 配药发药 ======
    Prescription dispensePrescription(Long prescriptionId, Long pharmacistId);
    IPage<Prescription> getPendingDispense(int page, int size);

    // ====== 处方前置审核 ======
    PrescriptionItem auditItem(Long itemId, String status, String note);

    // ====== 交易记录 ======
    IPage<DrugTransaction> getTransactions(Long drugId, String type, int page, int size);
}
