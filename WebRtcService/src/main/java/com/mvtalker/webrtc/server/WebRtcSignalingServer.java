package com.mvtalker.webrtc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mvtalker.webrtc.entity.data.*;
import com.mvtalker.webrtc.entity.BaseMessage;
import com.mvtalker.webrtc.entity.enums.MessageType;
import com.mvtalker.webrtc.interceptor.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/webrtc/signaling", configurator = WebSocketAuthInterceptor.class)
public class WebRtcSignalingServer
{
    // 语音流（媒体流）并不是通过信令服务器传输的，而是通过P2P网络直接在客户端之间传输。
    // 信令服务器仅用于交换建立P2P连接所需的元数据（如SDP、ICE候选等）

    // 存储客户端的连接对象,每个客户端连接都会产生一个连接对象
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();
    // 反向映射用于快速查找用户ID
    private static final ConcurrentHashMap<Session, Long> sessionToUserMap = new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void openWebSocketSession(Session session, EndpointConfig config)
    {
        // 是否能正确获取还有待测试
        Long userId = (Long) config.getUserProperties().get("userId");
        if (userId == null)
        {
            closeWithError(session, "未认证用户");
            return;
        }

        // 关闭旧连接（如果存在）
        Optional.ofNullable(sessionMap.get(userId)).ifPresent(oldSession ->
        {
            try
            {
                oldSession.close(new CloseReason(
                        CloseReason.CloseCodes.VIOLATED_POLICY,
                        "New connection replacing"
                ));
                log.debug("关闭旧连接 | userId={}", userId);
            }
            catch (IOException e)
            {
                log.error("关闭旧连接失败 | userId={}", userId, e);
            }
        });
        sessionMap.put(userId, session);
        sessionToUserMap.put(session, userId);
        log.debug("连接建立 | userId={} | sessionId={}", userId, session.getId());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void closeWebSocketSession(Session session, CloseReason reason)
    {
        Long userId = sessionToUserMap.get(session);
        if (userId != null)
        {
            // 原子性移除操作
            sessionMap.remove(userId);
            sessionToUserMap.remove(session);
            log.debug("连接关闭 | userId={} | reason={}", userId, reason);
        }
        else
        {
            log.warn("连接关闭失败 | sessionId={} | reason={}", session.getId(), reason);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void error(Session session, Throwable error)
    {
        log.error("WebSocket异常 | sessionId={}", session.getId(), error);
        error.printStackTrace();
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @OnMessage
    public void handleMessage(Session session, String message)
    {
        try
        {
            BaseMessage msg = mapper.readValue(message, BaseMessage.class);
            MessageType type = msg.getType();

            switch (type)
            {
                case offer:
                case answer:
                case iceCandidate:
                    handleSignaling(session, (SignalingMessage) msg);
                    break;

                // 处理心跳类型
                case heartbeat:
                    handleHeartbeat(session, (HeartbeatMessage) msg);
                    break;

                // 处理未知类型
                default:
                    sendError(session, "未知消息类型");
                    break;
            }
        }
        catch (JsonProcessingException e)
        {
            log.error("消息反序列化失败 | sessionId={} | rawMessage={}",
                    session.getId(),
                    message,
                    e
            );
            sendError(session, "消息格式错误");
        }
        catch (Exception e)
        {
            log.error("消息处理异常 | sessionId={}", session.getId(), e);
            sendError(session, "消息处理异常");
        }
    }

    private void send(Session session, BaseMessage message)
    {
        try
        {
            Long targetUserId = sessionToUserMap.get(session);
            String json = mapper.writeValueAsString(message);

            log.debug("发送消息 | toUserId={} | type={} | content={}",
                    targetUserId,
                    message.getType(),
                    json
            );

            session.getBasicRemote().sendText(json);
        }
        catch (Exception e)
        {
            log.error("消息发送失败 | toUserId={} | type={}",
                    sessionToUserMap.get(session),
                    message.getType(),
                    e
            );
        }
    }

    private void sendError(Session session, String errorMessage)
    {
        try
        {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setMessage(errorMessage);
            errorResponse.setType(MessageType.error);

            String json = mapper.writeValueAsString(errorResponse);

            session.getBasicRemote().sendText(json);
        }
        catch (Exception e)
        {
            log.error("消息序列化失败", e);
        }
    }

    private void closeWithError(Session session, String reason)
    {
        try
        {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,
                    reason
            ));
        }
        catch (IOException e)
        {
            log.error("关闭连接失败", e);
        }
    }

    public int getConnectNum()
    {
        return sessionMap.size();
    }

    private void handleHeartbeat(Session session, HeartbeatMessage msg)
    {
        HeartbeatData data = msg.getData();
        Long sourceUserId = data.getSourceUserId();

        // 验证用户身份
        Long currentUserId = sessionToUserMap.get(session);
        if (currentUserId == null || !currentUserId.equals(sourceUserId)) {
            sendError(session, "用户身份验证失败");
            return;
        }

        // 发送心跳响应
        HeartbeatMessage heartbeatResponse = new HeartbeatMessage();
        heartbeatResponse.setType(MessageType.heartbeat);
        heartbeatResponse.setTimestamp(LocalDateTime.now());
        heartbeatResponse.setData(new HeartbeatData()
        {
            {
                setMessage("PONG");
                setSourceUserId(currentUserId);
            }
        });
        send(session, heartbeatResponse);
    }

    private void handleSignaling(Session session,  SignalingMessage msg)
    {
        SignalingData data = msg.getData();
        Long sourceUserId = data.getSourceUserId();
        Long targetUserId = data.getTargetUserId();

        // 验证用户身份
        Long currentUserId = sessionToUserMap.get(session);
        if (currentUserId == null || !currentUserId.equals(sourceUserId)) {
            sendError(session, "用户身份验证失败");
            return;
        }

        // 转发消息
        Session targetSession = sessionMap.get(targetUserId);
        if (targetSession == null) {
            sendError(session, "对方不在线");
            return;
        }
        send(targetSession, msg);
    }

    // TODO: 心跳校验（超时删除等）
}
