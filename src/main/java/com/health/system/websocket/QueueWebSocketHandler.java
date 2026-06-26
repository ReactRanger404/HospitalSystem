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
 * 排队叫号 WebSocket 处理器
 * 向医生站和大屏实时推送叫号信息
 *
 * 前端连接示例:
 *   var socket = new SockJS('/api/ws/queue');
 *   socket.onmessage = function(e) { console.log(JSON.parse(e.data)); };
 *
 * @author health-system
 */
@Slf4j
@Component
public class QueueWebSocketHandler extends TextWebSocketHandler {

    /** 按科室分组管理 WebSocket 会话 <departmentId, session> */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long departmentId = extractDepartmentId(session);
        if (departmentId != null) {
            SESSIONS.put(departmentId, session);
            log.info("叫号WebSocket连接建立: departmentId={}, sessionId={}", departmentId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("收到叫号消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.values().remove(session);
        log.info("叫号WebSocket连接关闭: sessionId={}", session.getId());
    }

    /**
     * 向指定科室推送叫号信息
     * @param departmentId 科室ID
     * @param queueNumber  当前叫号
     * @param patientName  患者姓名
     * @param roomName     诊室名称
     */
    public static void notifyQueue(Long departmentId, int queueNumber, String patientName, String roomName) {
        WebSocketSession session = SESSIONS.get(departmentId);
        if (session != null && session.isOpen()) {
            try {
                String payload = String.format(
                        "{\"type\":\"CALL_NUMBER\",\"queueNumber\":%d,\"patientName\":\"%s\",\"roomName\":\"%s\"}",
                        queueNumber, patientName, roomName
                );
                session.sendMessage(new TextMessage(payload));
                log.info("推送叫号通知: departmentId={}, queueNumber={}", departmentId, queueNumber);
            } catch (IOException e) {
                log.error("推送叫号失败: ", e);
            }
        }
    }

    private Long extractDepartmentId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        if (query != null && query.contains("departmentId=")) {
            try {
                String[] params = query.split("&");
                for (String p : params) {
                    if (p.startsWith("departmentId=")) {
                        return Long.parseLong(p.split("=")[1]);
                    }
                }
            } catch (Exception e) {
                log.warn("解析departmentId失败: {}", query);
            }
        }
        return null;
    }
}
