package com.health.system.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 主题(Topic)配置
 * 定义系统各类消息的主题
 * 用途: 挂号通知、检查报告发布、处方审核等异步消息
 *
 * @author health-system
 */
@Configuration
public class KafkaConfig {

    /** 挂号预约通知主题 */
    public static final String TOPIC_APPOINTMENT = "topic-appointment";
    /** 报告发布通知主题 */
    public static final String TOPIC_REPORT_PUBLISH = "topic-report-publish";
    /** 处方审核通知主题 */
    public static final String TOPIC_PRESCRIPTION_AUDIT = "topic-prescription-audit";
    /** 系统日志主题 */
    public static final String TOPIC_SYSTEM_LOG = "topic-system-log";

    @Bean
    public NewTopic topicAppointment() {
        return TopicBuilder.name(TOPIC_APPOINTMENT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicReportPublish() {
        return TopicBuilder.name(TOPIC_REPORT_PUBLISH)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicPrescriptionAudit() {
        return TopicBuilder.name(TOPIC_PRESCRIPTION_AUDIT)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicSystemLog() {
        return TopicBuilder.name(TOPIC_SYSTEM_LOG)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
