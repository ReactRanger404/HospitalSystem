package com.health.system.service;

import com.health.system.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 消息生产者服务
 * 在关键业务节点发送异步消息，由消费者处理通知推送
 * Kafka 不可用时静默降级，不影响主业务流程
 *
 * @author health-system
 */
@Slf4j
@Service
public class MessageProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MessageProducerService(@Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 不可用，消息发送功能已降级（仅记录日志）");
        }
    }

    private boolean isKafkaAvailable() {
        if (kafkaTemplate == null) {
            log.debug("Kafka 未就绪，跳过消息发送");
            return false;
        }
        return true;
    }

    /**
     * 发送预约挂号通知
     */
    public void sendAppointmentNotification(Long patientId, Long doctorId,
                                            String patientName, String doctorName,
                                            String appointmentDate, String timeSlot) {
        if (!isKafkaAvailable()) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "APPOINTMENT_CREATED");
        msg.put("patientId", patientId);
        msg.put("patientName", patientName);
        msg.put("doctorId", doctorId);
        msg.put("doctorName", doctorName);
        msg.put("appointmentDate", appointmentDate);
        msg.put("timeSlot", timeSlot);
        msg.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.TOPIC_APPOINTMENT, String.valueOf(patientId), msg);
        log.info("已发送预约通知: patientId={}, doctorId={}", patientId, doctorId);
    }

    /**
     * 发送报告发布通知（推送医生端和患者端）
     */
    public void sendReportPublishedNotification(Long examRequestId, Long patientId,
                                                Long doctorId, String examName) {
        if (!isKafkaAvailable()) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "REPORT_PUBLISHED");
        msg.put("examRequestId", examRequestId);
        msg.put("patientId", patientId);
        msg.put("doctorId", doctorId);
        msg.put("examName", examName);
        msg.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.TOPIC_REPORT_PUBLISH, String.valueOf(patientId), msg);
        log.info("已发送报告发布通知: examRequestId={}", examRequestId);
    }

    /**
     * 发送处方审核结果通知
     */
    public void sendPrescriptionAuditNotification(Long prescriptionId, Long doctorId,
                                                  String auditStatus, String auditNote) {
        if (!isKafkaAvailable()) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "PRESCRIPTION_AUDIT");
        msg.put("prescriptionId", prescriptionId);
        msg.put("doctorId", doctorId);
        msg.put("auditStatus", auditStatus);
        msg.put("auditNote", auditNote);
        msg.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.TOPIC_PRESCRIPTION_AUDIT, String.valueOf(doctorId), msg);
        log.info("已发送处方审核通知: prescriptionId={}, status={}", prescriptionId, auditStatus);
    }

    /**
     * 发送系统操作日志
     */
    public void sendSystemLog(String module, String action, String detail, Long operatorId) {
        if (!isKafkaAvailable()) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("module", module);
        msg.put("action", action);
        msg.put("detail", detail);
        msg.put("operatorId", operatorId);
        msg.put("timestamp", System.currentTimeMillis());
        kafkaTemplate.send(KafkaConfig.TOPIC_SYSTEM_LOG, String.valueOf(operatorId), msg);
    }
}
