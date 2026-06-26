package com.health.system.consumer;

import com.health.system.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 消息消费者
 * 处理异步消息：预约通知、报告发布通知等
 *
 * @author health-system
 */
@Slf4j
@Component
public class NotificationConsumer {

    /**
     * 处理预约挂号通知
     * 可用于发送短信/微信通知、更新缓存等
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_APPOINTMENT, groupId = "hospital-appointment")
    public void handleAppointment(Map<String, Object> message) {
        log.info("收到预约通知: {}", message);
        // TODO: 发送短信/微信通知给患者和医生
        // String patientPhone = (String) message.get("patientPhone");
        // smsService.sendAppointmentConfirm(patientPhone, ...);
    }

    /**
     * 处理检查报告发布通知
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_REPORT_PUBLISH, groupId = "hospital-report")
    public void handleReportPublish(Map<String, Object> message) {
        log.info("收到报告发布通知: {}", message);
        // TODO: 推送报告到医生端和患者端
    }

    /**
     * 处理处方审核通知
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_PRESCRIPTION_AUDIT, groupId = "hospital-prescription")
    public void handlePrescriptionAudit(Map<String, Object> message) {
        log.info("收到处方审核通知: {}", message);
        // TODO: 通知医生审核结果
    }

    /**
     * 处理系统日志消息
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_SYSTEM_LOG, groupId = "hospital-log")
    public void handleSystemLog(Map<String, Object> message) {
        log.debug("系统日志: {}", message);
        // TODO: 持久化到日志数据库或ELK
    }
}
