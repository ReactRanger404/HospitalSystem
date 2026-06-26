# 医院综合信息管理系统 (Hospital Management System)

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 |
| ORM | MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis (Spring Cache) |
| 消息队列 | Kafka |
| 安全认证 | Spring Security + JWT |
| API文档 | Knife4j (Swagger) |
| 前端 | Vue 3 + Element Plus + Axios + Pinia |

## 项目结构

```
healthSystem/
├── pom.xml                          # Maven 依赖配置
├── sql/init.sql                     # 数据库初始化脚本（含演示数据）
├── start.bat                        # 一键启动脚本
├── src/main/java/com/health/system/
│   ├── HospitalApplication.java     # 启动类
│   ├── config/                      # 配置类（Redis/Kafka/Security/Swagger/MyBatisPlus）
│   ├── common/                      # 公共模块（JWT工具/过滤器/异常处理/统一返回值）
│   ├── entity/                      # 数据库实体（共16个表）
│   ├── mapper/                      # MyBatis-Plus Mapper 接口
│   ├── service/                     # 业务逻辑接口与实现
│   ├── controller/                  # RESTful 控制器
│   ├── dto/                         # 数据传输对象
│   ├── enums/                       # 枚举类
│   └── consumer/                    # Kafka 消息消费者
├── frontend/                        # Vue 3 前端
│   ├── src/views/                   # 各模块页面
│   └── package.json
```

## 模块功能

### 1️⃣ 门诊挂号与预约模块
- **医生排班管理**：创建各科室医生的出诊排班，管理号源数量
- **预约挂号**：科室 → 医生 → 时段选择，生成排队序号
- **签到到诊**：患者到院签到，自动进入排队队列
- **退号取消**：释放已占用的号源

### 2️⃣ 门诊医生站模块
- **电子病历（EMR）**：录入主诉、现病史、既往史、诊断等
- **处方开具**：西药/中药处方，支持用法用量设置
- **医技申请**：开具CT、核磁、B超等检查检验申请
- **历史调阅**：查看患者过往就诊记录、处方、检查报告

### 3️⃣ 医技工作站模块
- **登记接诊**：承接检查检验申请，安排患者检查
- **报告录入与审核**：录入检查结果，上级医生审核
- **报告发布**：审核通过后同步至医生端和患者端

### 4️⃣ 药房药库管理模块
- **药品进销存**：采购入库、库存盘点、临期预警
- **配药发药**：接收缴费处方，打印配药清单，FIFO扣减库存
- **处方前置审核**：自动/手动审核药品配伍禁忌与用量

### 5️⃣ 收费与财务结算模块
- **门诊收费**：挂号费、处方费、检查费合并结算（微信/支付宝/医保/现金）
- **住院结账**：入院登记、押金缴纳、出院结算
- **财务报表**：收入统计、科室收入、医生工作量

## 数据库表结构（16张表）

| 表名 | 说明 |
|------|------|
| departments | 科室信息 |
| users | 用户（医生/护士/患者/管理员） |
| doctor_schedules | 医生排班 |
| appointments | 预约挂号 |
| medical_records | 电子病历 |
| prescriptions | 处方 |
| prescription_items | 处方明细 |
| exam_requests | 检查检验申请 |
| exam_results | 检查检验结果 |
| drugs | 药品基本信息 |
| drug_inventories | 药品库存（批次） |
| drug_transactions | 药品出入库记录 |
| bills | 账单 |
| bill_items | 账单明细 |
| payments | 支付记录 |
| inpatient_accounts | 住院账户 |

## 启动方式

### 前置条件
- JDK 21+
- MySQL 8.0+
- Redis (可选，用于缓存)
- Kafka (可选，用于消息通知)
- Node.js 18+ (用于前端开发)

### 1. 数据库初始化
```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动后端
```bash
mvn spring-boot:run
# 或
mvn clean package -DskipTests
java -jar target/hospital-system-1.0.0.jar
```

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

### 4. 访问地址
- 前端页面：http://localhost:3000
- 后端API：http://localhost:8080/api
- Swagger文档：http://localhost:8080/api/doc.html

## 演示账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 管理员 | 系统管理 |
| zhangsan | 123456 | 医生 | 内科主任医师 |
| lisi | 123456 | 医生 | 内科副主任医师 |
| wangwu | 123456 | 医生 | 外科主治医师 |
| nurse01 | 123456 | 护士 | 内科护士 |
| tech01 | 123456 | 医技 | 放射科技师 |
| pharm01 | 123456 | 药剂师 | 药房 |
| patient01 | 123456 | 患者 | 就诊患者 |

## 关键设计

### 缓存策略（Redis）
- 科室列表缓存：`departments:active`
- 用户信息缓存：`user:{username}`
- 排班缓存：`schedules:department:{id}`

### 消息队列（Kafka）
- `topic-appointment`: 预约挂号通知
- `topic-report-publish`: 检查报告发布通知
- `topic-prescription-audit`: 处方审核通知
- `topic-system-log`: 系统日志

### 安全认证（JWT）
- 使用 BCrypt 加密存储密码
- JWT 令牌有效期 8 小时
- 基于 Spring Security 的角色权限控制
