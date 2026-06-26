package com.health.system.config;

import com.health.system.websocket.QueueWebSocketHandler;
import com.health.system.websocket.ReportWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 * 实现排队叫号、报告通知等实时推送功能
 *
 * @author health-system
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 排队叫号 WebSocket：医生站/大屏显示实时叫号
        registry.addHandler(queueWebSocketHandler(), "/ws/queue")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 报告通知 WebSocket：报告发布实时推送给医生/患者
        registry.addHandler(reportWebSocketHandler(), "/ws/report")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Bean
    public QueueWebSocketHandler queueWebSocketHandler() {
        return new QueueWebSocketHandler();
    }

    @Bean
    public ReportWebSocketHandler reportWebSocketHandler() {
        return new ReportWebSocketHandler();
    }
}
