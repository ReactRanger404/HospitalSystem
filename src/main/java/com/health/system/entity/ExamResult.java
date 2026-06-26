package com.health.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 检查检验结果实体
 * 记录医技人员录入的检查报告、医学影像描述及审核信息
 *
 * @author health-system
 */
@Data
@TableName("exam_results")
public class ExamResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联申请ID */
    private Long examRequestId;

    /** 操作技师ID */
    private Long technicianId;

    /** 审核医生ID */
    private Long reviewerId;

    /** 结果描述 */
    private String resultDescription;

    /** 检查结论 */
    private String conclusion;

    /** 参考范围 */
    private String referenceRange;

    /** 异常标识 */
    private String abnormalFlags;

    /** 影像图片路径（逗号分隔） */
    private String imageUrls;

    /** 检查设备 */
    private String equipment;

    /** 状态: draft/submitted/reviewed/published */
    private String status;

    /** 报告文件路径 */
    private String reportFileUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 提交时间 */
    private LocalDateTime submittedAt;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    /** 发布时间 */
    private LocalDateTime publishedAt;
}
