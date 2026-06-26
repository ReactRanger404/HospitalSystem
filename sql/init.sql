-- =============================================================
-- 医院综合信息管理系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 说明: 创建数据库、表结构和初始演示数据
-- =============================================================

-- 创建数据库
DROP DATABASE IF EXISTS health_system;
CREATE DATABASE health_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE health_system;

-- =============================================================
-- 1. 科室表
-- =============================================================
CREATE TABLE departments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '科室ID',
    name        VARCHAR(100) NOT NULL COMMENT '科室名称',
    code        VARCHAR(20) NOT NULL COMMENT '科室编码',
    category    VARCHAR(50) NOT NULL COMMENT '科室类别(临床/医技/药房/行政)',
    description TEXT COMMENT '科室简介',
    location    VARCHAR(200) COMMENT '科室位置',
    phone       VARCHAR(20) COMMENT '联系电话',
    is_active   TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室信息表';

-- =============================================================
-- 2. 用户表（医生、护士、患者、管理员统一管理）
-- =============================================================
CREATE TABLE users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username          VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password          VARCHAR(200) NOT NULL COMMENT '密码哈希(BCrypt)',
    real_name         VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role              VARCHAR(20) NOT NULL COMMENT '角色(doctor/nurse/patient/admin/tech/pharmacist)',
    gender            VARCHAR(10) COMMENT '性别',
    phone             VARCHAR(20) COMMENT '手机号',
    email             VARCHAR(100) COMMENT '电子邮箱',
    id_card           VARCHAR(18) COMMENT '身份证号',
    birth_date        DATE COMMENT '出生日期',
    address           VARCHAR(200) COMMENT '家庭住址',
    department_id     BIGINT COMMENT '所属科室ID',
    title             VARCHAR(50) COMMENT '职称',
    license_number    VARCHAR(50) COMMENT '执业证号',
    specialization    VARCHAR(200) COMMENT '专业特长',
    blood_type        VARCHAR(10) COMMENT '血型',
    allergies         TEXT COMMENT '过敏史',
    medical_history   TEXT COMMENT '既往病史摘要',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone   VARCHAR(20) COMMENT '紧急联系电话',
    is_active         TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_department (department_id),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- =============================================================
-- 3. 医生排班表
-- =============================================================
CREATE TABLE doctor_schedules (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '排班ID',
    doctor_id       BIGINT NOT NULL COMMENT '医生ID',
    department_id   BIGINT NOT NULL COMMENT '科室ID',
    schedule_date   DATE NOT NULL COMMENT '出诊日期',
    time_slot       VARCHAR(20) NOT NULL COMMENT '时段(morning/afternoon/evening)',
    start_time      TIME COMMENT '开始时间',
    end_time        TIME COMMENT '结束时间',
    max_patients    INT DEFAULT 30 COMMENT '最大号源数',
    booked_count    INT DEFAULT 0 COMMENT '已预约数',
    is_active       TINYINT(1) DEFAULT 1 COMMENT '是否有效',
    remark          VARCHAR(200) COMMENT '备注',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_doctor_date (doctor_id, schedule_date),
    KEY idx_dept_date (department_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- =============================================================
-- 4. 预约挂号表
-- =============================================================
CREATE TABLE appointments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    patient_id        BIGINT NOT NULL COMMENT '患者ID',
    doctor_id         BIGINT NOT NULL COMMENT '医生ID',
    department_id     BIGINT NOT NULL COMMENT '科室ID',
    schedule_id       BIGINT NOT NULL COMMENT '排班ID',
    appointment_date  DATE NOT NULL COMMENT '预约日期',
    time_slot         VARCHAR(20) NOT NULL COMMENT '预约时段',
    queue_number      INT COMMENT '排队序号',
    status            VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/checked_in/in_consultation/completed/cancelled/refunded)',
    source            VARCHAR(20) DEFAULT 'onsite' COMMENT '挂号来源(wechat/app/kiosk/onsite)',
    symptoms          TEXT COMMENT '症状描述',
    is_first_visit    TINYINT(1) DEFAULT 1 COMMENT '是否初诊',
    cancel_reason     VARCHAR(200) COMMENT '取消原因',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_patient (patient_id),
    KEY idx_doctor_date (doctor_id, appointment_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约挂号表';

-- =============================================================
-- 5. 电子病历表
-- =============================================================
CREATE TABLE medical_records (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '病历ID',
    patient_id            BIGINT NOT NULL COMMENT '患者ID',
    doctor_id             BIGINT NOT NULL COMMENT '医生ID',
    appointment_id        BIGINT COMMENT '关联预约ID',
    visit_date            DATE NOT NULL COMMENT '就诊日期',
    visit_type            VARCHAR(20) DEFAULT 'outpatient' COMMENT '就诊类型(outpatient/inpatient/emergency)',
    chief_complaint       TEXT COMMENT '主诉',
    present_illness       TEXT COMMENT '现病史',
    past_history          TEXT COMMENT '既往史',
    personal_history      TEXT COMMENT '个人史',
    family_history        TEXT COMMENT '家族史',
    allergy_history       TEXT COMMENT '过敏史',
    physical_examination  TEXT COMMENT '体格检查',
    vital_signs           VARCHAR(500) COMMENT '生命体征(JSON)',
    diagnosis             TEXT COMMENT '诊断结果',
    diagnosis_code        VARCHAR(100) COMMENT '诊断编码(ICD-10)',
    treatment_plan        TEXT COMMENT '治疗方案',
    doctor_advice         TEXT COMMENT '医生建议',
    follow_up             VARCHAR(200) COMMENT '随访建议',
    is_finalized          TINYINT(1) DEFAULT 0 COMMENT '是否已归档',
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_patient (patient_id),
    KEY idx_doctor (doctor_id),
    KEY idx_visit_date (visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子病历表';

-- =============================================================
-- 6. 处方表
-- =============================================================
CREATE TABLE prescriptions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '处方ID',
    patient_id          BIGINT NOT NULL COMMENT '患者ID',
    doctor_id           BIGINT NOT NULL COMMENT '医生ID',
    medical_record_id   BIGINT COMMENT '关联病历ID',
    prescription_type   VARCHAR(20) NOT NULL COMMENT '处方类型(western/chinese)',
    status              VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/paid/dispensing/dispensed/cancelled)',
    decoction_method    VARCHAR(200) COMMENT '煎药方法',
    decoction_note      TEXT COMMENT '煎药说明',
    total_amount        DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    remark              TEXT COMMENT '备注',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_patient (patient_id),
    KEY idx_doctor (doctor_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- =============================================================
-- 7. 处方明细表
-- =============================================================
CREATE TABLE prescription_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    prescription_id     BIGINT NOT NULL COMMENT '处方ID',
    drug_id             BIGINT NOT NULL COMMENT '药品ID',
    drug_name           VARCHAR(200) NOT NULL COMMENT '药品名称',
    specification       VARCHAR(100) COMMENT '药品规格',
    dosage              VARCHAR(50) NOT NULL COMMENT '单次用量',
    frequency           VARCHAR(50) NOT NULL COMMENT '使用频次',
    frequency_detail    VARCHAR(200) COMMENT '频次说明',
    days                INT DEFAULT 1 COMMENT '用药天数',
    quantity            INT NOT NULL COMMENT '数量',
    unit                VARCHAR(20) COMMENT '单位(盒/瓶/袋)',
    unit_price          DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    amount              DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    usage_method        VARCHAR(50) COMMENT '给药途径(oral/injection/external)',
    usage_instruction   VARCHAR(200) COMMENT '用药指导',
    day_pattern         VARCHAR(20) DEFAULT 'daily' COMMENT '用药模式(daily/odd/even)',
    note                VARCHAR(200) COMMENT '备注',
    audit_status        VARCHAR(20) DEFAULT 'pending' COMMENT '前置审核状态(pending/passed/rejected)',
    audit_note          VARCHAR(200) COMMENT '审核意见',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_prescription (prescription_id),
    KEY idx_drug (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- =============================================================
-- 8. 检查检验申请表
-- =============================================================
CREATE TABLE exam_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    patient_id          BIGINT NOT NULL COMMENT '患者ID',
    doctor_id           BIGINT NOT NULL COMMENT '开单医生ID',
    medical_record_id   BIGINT COMMENT '关联病历ID',
    request_type        VARCHAR(20) NOT NULL COMMENT '申请类型(examination/lab_test)',
    exam_category       VARCHAR(50) NOT NULL COMMENT '检查检验类别',
    exam_name           VARCHAR(200) NOT NULL COMMENT '项目名称',
    status              VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/in_progress/completed/cancelled)',
    clinical_diagnosis  TEXT COMMENT '临床诊断',
    clinical_note       TEXT COMMENT '临床备注',
    urgency             VARCHAR(20) DEFAULT 'routine' COMMENT '紧急程度(emergency/urgent/routine)',
    is_outpatient       TINYINT(1) DEFAULT 1 COMMENT '是否门诊',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_patient (patient_id),
    KEY idx_doctor (doctor_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查检验申请表';

-- =============================================================
-- 9. 检查检验结果表
-- =============================================================
CREATE TABLE exam_results (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID',
    exam_request_id     BIGINT NOT NULL COMMENT '关联申请ID',
    technician_id       BIGINT COMMENT '操作技师ID',
    reviewer_id         BIGINT COMMENT '审核医生ID',
    result_description  TEXT COMMENT '结果描述',
    conclusion          TEXT COMMENT '检查结论',
    reference_range     TEXT COMMENT '参考范围',
    abnormal_flags      VARCHAR(500) COMMENT '异常标识',
    image_urls          TEXT COMMENT '影像图片路径(逗号分隔)',
    equipment           VARCHAR(200) COMMENT '检查设备',
    status              VARCHAR(20) DEFAULT 'draft' COMMENT '状态(draft/submitted/reviewed/published)',
    report_file_url     VARCHAR(500) COMMENT '报告文件路径',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    submitted_at        DATETIME COMMENT '提交时间',
    reviewed_at         DATETIME COMMENT '审核时间',
    published_at        DATETIME COMMENT '发布时间',
    UNIQUE KEY uk_exam_request (exam_request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查检验结果表';

-- =============================================================
-- 10. 药品基本信息表
-- =============================================================
CREATE TABLE drugs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '药品ID',
    code                VARCHAR(50) NOT NULL COMMENT '药品编码',
    name                VARCHAR(200) NOT NULL COMMENT '药品名称',
    generic_name        VARCHAR(200) COMMENT '通用名',
    english_name        VARCHAR(200) COMMENT '英文名',
    drug_category       VARCHAR(30) NOT NULL COMMENT '药品分类(western/chinese/chinese_patent)',
    dosage_form         VARCHAR(30) COMMENT '剂型(tablet/capsule/injection)',
    specification       VARCHAR(100) COMMENT '药品规格',
    unit                VARCHAR(20) COMMENT '单位(盒/瓶/袋)',
    manufacturer        VARCHAR(200) COMMENT '生产厂家',
    approval_number     VARCHAR(100) COMMENT '批准文号',
    purchase_price      DECIMAL(10,2) DEFAULT 0.00 COMMENT '采购单价',
    sale_price          DECIMAL(10,2) DEFAULT 0.00 COMMENT '零售单价',
    min_stock           INT DEFAULT 10 COMMENT '最低库存预警',
    max_stock           INT DEFAULT 500 COMMENT '最高库存上限',
    is_prescription     TINYINT(1) DEFAULT 1 COMMENT '是否处方药',
    is_active           TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    remark              TEXT COMMENT '备注',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_code (code),
    KEY idx_category (drug_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品信息表';

-- =============================================================
-- 11. 药品库存表
-- =============================================================
CREATE TABLE drug_inventories (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    drug_id             BIGINT NOT NULL COMMENT '药品ID',
    batch_number        VARCHAR(100) NOT NULL COMMENT '批号',
    quantity            INT DEFAULT 0 COMMENT '当前库存数量',
    purchase_price      DECIMAL(10,2) DEFAULT 0.00 COMMENT '采购单价',
    sale_price          DECIMAL(10,2) DEFAULT 0.00 COMMENT '零售单价',
    production_date     DATE COMMENT '生产日期',
    expiry_date         DATE NOT NULL COMMENT '有效期至',
    supplier            VARCHAR(200) COMMENT '供应商',
    location            VARCHAR(100) COMMENT '货位',
    is_active           TINYINT(1) DEFAULT 1 COMMENT '是否有效',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_drug (drug_id),
    KEY idx_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品库存表';

-- =============================================================
-- 12. 药品出入库记录表
-- =============================================================
CREATE TABLE drug_transactions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交易ID',
    drug_id             BIGINT NOT NULL COMMENT '药品ID',
    inventory_id        BIGINT COMMENT '批次库存ID',
    transaction_type    VARCHAR(20) NOT NULL COMMENT '交易类型(purchase/dispense/return/transfer/adjustment)',
    quantity            INT NOT NULL COMMENT '数量(正数入库/负数出库)',
    unit_price          DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    total_price         DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    operator_id         BIGINT COMMENT '操作人ID',
    reference_type      VARCHAR(30) COMMENT '关联单据类型',
    reference_id        BIGINT COMMENT '关联单据ID',
    note                VARCHAR(500) COMMENT '备注',
    transaction_date    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_drug (drug_id),
    KEY idx_type (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品出入库记录表';

-- =============================================================
-- 13. 账单表
-- =============================================================
CREATE TABLE bills (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
    bill_no             VARCHAR(50) NOT NULL COMMENT '账单编号',
    patient_id          BIGINT NOT NULL COMMENT '患者ID',
    bill_type           VARCHAR(30) NOT NULL COMMENT '账单类型(registration/prescription/examination/hospitalization)',
    reference_id        BIGINT COMMENT '关联业务ID',
    reference_type      VARCHAR(30) COMMENT '关联业务类型',
    total_amount        DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    discount_amount     DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    payable_amount      DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
    paid_amount         DECIMAL(12,2) DEFAULT 0.00 COMMENT '已付金额',
    status              VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/partially_paid/paid/refunded/cancelled)',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at             DATETIME COMMENT '支付时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_patient (patient_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- =============================================================
-- 14. 账单明细表
-- =============================================================
CREATE TABLE bill_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    bill_id             BIGINT NOT NULL COMMENT '账单ID',
    item_type           VARCHAR(30) NOT NULL COMMENT '项目类型(registration/prescription/examination)',
    item_id             BIGINT COMMENT '项目ID',
    item_name           VARCHAR(200) NOT NULL COMMENT '项目名称',
    quantity            INT DEFAULT 1 COMMENT '数量',
    unit_price          DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    amount              DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单明细表';

-- =============================================================
-- 15. 支付记录表
-- =============================================================
CREATE TABLE payments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
    bill_id             BIGINT NOT NULL COMMENT '账单ID',
    payment_no          VARCHAR(100) NOT NULL COMMENT '支付流水号',
    amount              DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    payment_method      VARCHAR(30) NOT NULL COMMENT '支付方式(wechat/alipay/insurance/cash/card)',
    payment_type        VARCHAR(20) DEFAULT 'full' COMMENT '支付类型(full/partial/deposit/refund)',
    transaction_id      VARCHAR(200) COMMENT '第三方交易号',
    status              VARCHAR(20) DEFAULT 'success' COMMENT '状态(pending/success/failed/refunded)',
    operator_id         BIGINT COMMENT '操作员ID',
    note                VARCHAR(200) COMMENT '备注',
    paid_at             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_bill (bill_id),
    KEY idx_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- =============================================================
-- 16. 住院账户表
-- =============================================================
CREATE TABLE inpatient_accounts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    patient_id          BIGINT NOT NULL COMMENT '患者ID',
    admission_number    VARCHAR(50) NOT NULL COMMENT '住院号',
    ward_number         VARCHAR(20) COMMENT '病房号',
    bed_number          VARCHAR(20) COMMENT '床位号',
    admission_date      DATETIME NOT NULL COMMENT '入院日期',
    discharge_date      DATETIME COMMENT '出院日期',
    deposit_amount      DECIMAL(12,2) DEFAULT 0.00 COMMENT '押金总额',
    total_expenses      DECIMAL(12,2) DEFAULT 0.00 COMMENT '总费用',
    balance             DECIMAL(12,2) DEFAULT 0.00 COMMENT '余额(正数为欠费)',
    status              VARCHAR(20) DEFAULT 'admitted' COMMENT '状态(admitted/discharged/settled)',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_admission_no (admission_number),
    KEY idx_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院账户表';

-- =============================================================
-- 补充索引（提升高频查询性能）
-- =============================================================
ALTER TABLE users ADD INDEX idx_phone (phone);
ALTER TABLE users ADD INDEX idx_id_card (id_card);
ALTER TABLE appointments ADD INDEX idx_date_status (appointment_date, status);
ALTER TABLE medical_records ADD INDEX idx_doctor_date (doctor_id, visit_date);
ALTER TABLE prescriptions ADD INDEX idx_doctor_patient (doctor_id, patient_id);
ALTER TABLE exam_results ADD INDEX idx_status (status);
ALTER TABLE exam_results ADD INDEX idx_published_at (published_at);
ALTER TABLE bills ADD INDEX idx_created_at (created_at);
ALTER TABLE bills ADD INDEX idx_patient_status (patient_id, status);
ALTER TABLE drug_inventories ADD INDEX idx_drug_batch (drug_id, batch_number);
ALTER TABLE drug_inventories ADD INDEX idx_expiry_date (expiry_date);

-- =============================================================
-- 初始演示数据
-- =============================================================

-- 密码均为 123456 (BCrypt加密)
INSERT INTO departments (name, code, category, location) VALUES
('内科', 'NK', '临床', '门诊楼2层'),
('外科', 'WK', '临床', '门诊楼2层'),
('儿科', 'EK', '临床', '门诊楼3层'),
('妇产科', 'FCK', '临床', '门诊楼3层'),
('骨科', 'GK', '临床', '门诊楼2层'),
('眼科', 'YK', '临床', '门诊楼4层'),
('耳鼻喉科', 'EBHK', '临床', '门诊楼4层'),
('皮肤科', 'PFK', '临床', '门诊楼4层'),
('中医科', 'ZYK', '临床', '门诊楼5层'),
('急诊科', 'JZK', '临床', '急诊楼1层'),
('放射科', 'FSK', '医技', '医技楼1层'),
('检验科', 'JYK', '医技', '医技楼2层'),
('超声科', 'CSK', '医技', '医技楼2层'),
('药房', 'YF', '药房', '门诊楼1层');

INSERT INTO users (username, password, real_name, role, gender, phone, department_id, title) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin', '男', '13800000000', NULL, '管理员'),

('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'doctor', '男', '13800138001', 1, '主任医师'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 'doctor', '女', '13800138002', 1, '副主任医师'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王五', 'doctor', '男', '13800138003', 2, '主治医师'),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '赵六', 'doctor', '女', '13800138004', 3, '主任医师'),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '孙七', 'doctor', '男', '13800138005', 4, '副主任医师'),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '周八', 'doctor', '男', '13800138006', 11, '副主任医师'),

('nurse01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '刘护士', 'nurse', '女', '13800138101', 1, '主管护师'),
('nurse02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '陈护士', 'nurse', '女', '13800138102', 2, '护师'),

('tech01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '吴技师', 'tech', '男', '13800138201', 11, '主管技师'),
('tech02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '郑技师', 'tech', '女', '13800138202', 12, '技师'),

('pharm01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '马药师', 'pharmacist', '女', '13800138301', 14, '主管药师'),

('patient01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王小明', 'patient', '男', '13900000001', NULL, NULL),
('patient02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李小红', 'patient', '女', '13900000002', NULL, NULL),
('patient03', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张大爷', 'patient', '男', '13900000003', NULL, NULL);

-- 演示药品数据
INSERT INTO drugs (code, name, generic_name, drug_category, dosage_form, specification, unit, manufacturer, sale_price, min_stock, is_prescription) VALUES
('WM0001', '阿莫西林胶囊', '阿莫西林', 'western', 'capsule', '0.5g*24粒', '盒', '华北制药', 12.50, 50, TRUE),
('WM0002', '布洛芬缓释胶囊', '布洛芬', 'western', 'capsule', '0.3g*20粒', '盒', '中美史克', 18.00, 50, FALSE),
('WM0003', '盐酸二甲双胍片', '盐酸二甲双胍', 'western', 'tablet', '0.5g*60片', '瓶', '施贵宝', 25.00, 30, TRUE),
('WM0004', '硝苯地平控释片', '硝苯地平', 'western', 'tablet', '30mg*12片', '盒', '拜耳', 35.00, 30, TRUE),
('WM0005', '头孢克肟分散片', '头孢克肟', 'western', 'tablet', '100mg*6片', '盒', '广州白云山', 22.00, 50, TRUE),
('WM0006', '氯雷他定片', '氯雷他定', 'western', 'tablet', '10mg*6片', '盒', '先灵葆雅', 15.00, 40, FALSE),
('WM0007', '奥美拉唑肠溶胶囊', '奥美拉唑', 'western', 'capsule', '20mg*14粒', '盒', '阿斯利康', 45.00, 30, TRUE),
('ZY0001', '板蓝根颗粒', '板蓝根', 'chinese', 'granule', '10g*20袋', '袋', '同仁堂', 15.00, 100, FALSE),
('ZY0002', '藿香正气水', '藿香正气', 'chinese', 'liquid', '10ml*10支', '盒', '太极集团', 12.00, 50, FALSE),
('ZY0003', '六味地黄丸', '六味地黄', 'chinese_patent', 'pill', '360丸', '瓶', '仲景宛西', 28.00, 40, FALSE),
('ZY0004', '阿胶', '阿胶', 'chinese', 'block', '250g', '盒', '东阿阿胶', 650.00, 10, FALSE);
