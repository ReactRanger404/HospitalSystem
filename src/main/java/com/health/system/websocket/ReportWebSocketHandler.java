package com.health.system.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 检查报告发布 WebSocket 处理器
 * 报告审核发布后实时推送给开单医生和患者端
 *
 * @author health-system
 */
@Slf4j
@Component
public class ReportWebSocketHandler extends TextWebSocketHandler {

    /** <doctorId/patientId, session> */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("报告WebSocket连接建立: userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.values().remove(session);
    }

    /**
     * 推送报告发布通知给指定用户
     * @param targetId  目标用户ID（医生/患者）
     * @param reportInfo JSON格式的报告信息
     */
    public static void notifyReportPublished(String targetId, String reportInfo) {
        WebSocketSession session = SESSIONS.get(targetId);
        if (session != null && session.isOpen()) {
            try {
                String payload = String.format(
                        "{\"type\":\"REPORT_PUBLISHED\",\"data\":%s}", reportInfo
                );
                session.sendMessage(new TextMessage(payload));
                log.info("推送报告通知: userId={}", targetId);
            } catch (IOException e) {
                log.error("推送报告通知失败: ", e);
            }
        }
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query != null && query.contains("userId=")) {
            try {
                String[] params = query.split("&");
                for (String p : params) {
                    if (p.startsWith("userId=")) return p.split("=")[1];
                }
            } catch (Exception e) {
                log.warn("解析userId失败: {}", query);
            }
        }
        return null;
    }
}
