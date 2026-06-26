package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院账户实体
 * 管理住院押金、每日清单查询以及出院清算
 *
 * @author health-system
 */
@Data
@TableName("inpatient_accounts")
public class InpatientAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 住院号 */
    private String admissionNumber;

    /** 病房号 */
    private String wardNumber;

    /** 床位号 */
    private String bedNumber;

    /** 入院日期 */
    private LocalDateTime admissionDate;

    /** 出院日期 */
    private LocalDateTime dischargeDate;

    /** 押金总额 */
    private BigDecimal depositAmount;

    /** 总费用 */
    private BigDecimal totalExpenses;

    /** 余额（正数为欠费） */
    private BigDecimal balance;

    /** 状态: admitted/discharged/settled */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
