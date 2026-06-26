package com.health.system.consumer;

import com.health.system.config.KafkaConfig;
import com.health.system.websocket.QueueWebSocketHandler;
import com.health.system.websocket.ReportWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 消息消费者
 * 消费异步消息并执行实际动作：WebSocket推送、日志记录等
 *
 * @author health-system
 */
@Slf4j
@Component
@ConditionalOnBean(KafkaTemplate.class)  // Kafka不可用时跳过消费者
public class NotificationConsumer {

    /**
     * 预约通知：通过 WebSocket 向对应科室推送叫号更新
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_APPOINTMENT, groupId = "hospital-appointment")
    public void handleAppointment(Map<String, Object> msg) {
        log.info("消费预约通知: {}", msg);
        try {
            String type = (String) msg.get("type");
            if ("APPOINTMENT_CREATED".equals(type)) {
                // 向科室推送排队队列更新
                Long departmentId = msg.get("departmentId") != null
                        ? Long.valueOf(msg.get("departmentId").toString()) : null;
                String patientName = (String) msg.get("patientName");
                if (departmentId != null && patientName != null) {
                    QueueWebSocketHandler.notifyQueue(departmentId, 0,
                            patientName + " 已预约", "待分配诊室");
                }
            }
        } catch (Exception e) {
            log.error("处理预约通知异常", e);
        }
    }

    /**
     * 报告发布通知：通过 WebSocket 推送给开单医生和患者
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_REPORT_PUBLISH, groupId = "hospital-report")
    public void handleReportPublish(Map<String, Object> msg) {
        log.info("消费报告发布通知: {}", msg);
        try {
            String type = (String) msg.get("type");
            if ("REPORT_PUBLISHED".equals(type)) {
                // 推送给开单医生
                Long doctorId = msg.get("doctorId") != null
                        ? Long.valueOf(msg.get("doctorId").toString()) : null;
                // 推送给患者
                Long patientId = msg.get("patientId") != null
                        ? Long.valueOf(msg.get("patientId").toString()) : null;

                String reportJson = String.format(
                        "{\"examRequestId\":%s,\"examName\":\"%s\"}",
                        msg.get("examRequestId"), msg.get("examName")
                );

                if (doctorId != null) {
                    ReportWebSocketHandler.notifyReportPublished(
                            "doctor_" + doctorId, reportJson);
                }
                if (patientId != null) {
                    ReportWebSocketHandler.notifyReportPublished(
                            "patient_" + patientId, reportJson);
                }
            }
        } catch (Exception e) {
            log.error("处理报告发布通知异常", e);
        }
    }

    /**
     * 处方审核通知：推送给开单医生
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_PRESCRIPTION_AUDIT, groupId = "hospital-prescription")
    public void handlePrescriptionAudit(Map<String, Object> msg) {
        log.info("消费处方审核通知: {}", msg);
        try {
            String type = (String) msg.get("type");
            if ("PRESCRIPTION_AUDIT".equals(type)) {
                Long doctorId = msg.get("doctorId") != null
                        ? Long.valueOf(msg.get("doctorId").toString()) : null;
                if (doctorId != null) {
                    String auditInfo = String.format(
                            "{\"prescriptionId\":%s,\"status\":\"%s\"}",
                            msg.get("prescriptionId"), msg.get("auditStatus")
                    );
                    ReportWebSocketHandler.notifyReportPublished(
                            "doctor_" + doctorId, auditInfo);
                }
            }
        } catch (Exception e) {
            log.error("处理处方审核通知异常", e);
        }
    }

    /**
     * 系统日志：写入日志文件（已由 logback 处理）
     * 生产环境可扩展为写入 ELK 或日志表
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_SYSTEM_LOG, groupId = "hospital-log")
    public void handleSystemLog(Map<String, Object> msg) {
        if (log.isDebugEnabled()) {
            log.debug("系统操作日志: module={}, action={}, operatorId={}",
                    msg.get("module"), msg.get("action"), msg.get("operatorId"));
        }
    }
}
