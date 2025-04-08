package com.mvtalker.webrtc.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvtalker.utilities.common.UserContext;
import com.mvtalker.webrtc.entity.WebRtcSignalingMessage;
import com.mvtalker.webrtc.entity.data.*;
import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/webrtc/signaling")
public class WebRtcSignalingServer
{
    // 语音流（媒体流）并不是通过信令服务器传输的，而是通过P2P网络直接在客户端之间传输。
    // 信令服务器仅用于交换建立P2P连接所需的元数据（如SDP、ICE候选等）

    // 存储客户端的连接对象,每个客户端连接都会产生一个连接对象
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();
    // 反向映射用于快速查找用户ID
    private static final ConcurrentHashMap<Session, Long> sessionToUserMap = new ConcurrentHashMap<>();

    // TODO: 实现心跳机制

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void openWebSocketSession(Session session)
    {
        // 是否能正确获取还有待测试
        Long userId = UserContext.getUserId();
        // 关闭旧连接（如果存在）
        Optional.ofNullable(sessionMap.get(userId)).ifPresent(oldSession -> {
            try
            {
                oldSession.close(new CloseReason(
                        CloseReason.CloseCodes.VIOLATED_POLICY,
                        "New connection replacing"
                ));
            }
            catch (IOException e) {
                log.error("关闭旧连接失败 userId={}", userId, e);
            }
        });
        sessionMap.put(userId, session);
        sessionToUserMap.put(session, userId);
        log.debug("WS连接建立 | userId={} | sessionId={}", userId, session.getId());
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
            sessionMap.remove(userId, session);
            log.debug("WS连接关闭 | userId={} | reason={}", userId, reason);
        }
        else
        {
            log.warn("WS连接关闭失败 | sessionId={} | reason={}", session.getId(), reason);
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
            ObjectMapper mapper = new ObjectMapper();
            WebRtcSignalingMessage<?> msg = mapper.readValue(message, WebRtcSignalingMessage.class);
            Long fromUser = UserContext.getUserId();
            Long toUser = extractTargetUserId(msg);

            Session toSession = sessionMap.get(toUser);
            if (toSession == null)
            {
                sendError(session, "用户不在线");
                return;
            }

            switch (msg.getType())
            {
                case OFFER:
                    handleOffer(session, toSession, (OfferData) msg.getData());
                    break;
                case ICE_CANDIDATE:
                    handleIceCandidate(session, toSession, (IceCandidate) msg.getData());
                    break;
                default:
                    sendError(session, "不支持的消息类型");
            }
        } catch (Exception e) {
            log.error("处理失败", e);
            sendError(session, "消息处理异常");
        }
    }

    private Long extractTargetUserId(WebRtcSignalingMessage<?> message)
    {
        // 假设消息数据包含目标用户ID（需根据实际数据结构调整）
        if (message.getData() instanceof SignalingData)
        {
            return ((SignalingData) message.getData()).getTargetUserId();
        }
        // 其他类型处理，例如从消息头获取
        throw new IllegalArgumentException("消息数据类型不支持");
    }

    private void handleOffer(Session fromSession, Session toSession, OfferData offerData)
    {
        // 构建消息并转发
        WebRtcSignalingMessage<OfferData> forwardMsg = new WebRtcSignalingMessage<>();
        forwardMsg.setType(MessageType.OFFER);
        forwardMsg.setData(offerData);
        send(toSession, forwardMsg);
    }

    private void send(Session session, WebRtcSignalingMessage<?> message)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(message);
            session.getBasicRemote().sendText(json);
        }
        catch (Exception e)
        {
            log.error("发送消息失败", e);
        }
    }

    private void handleIceCandidate(Session fromSession, Session toSession, IceCandidate candidate)
    {
        WebRtcSignalingMessage<IceCandidate> msg = new WebRtcSignalingMessage<>();
        msg.setType(MessageType.ICE_CANDIDATE);
        msg.setData(candidate);
        send(toSession, msg);
    }

    private void sendError(Session session, String errorMessage)
    {
        ErrorData errorData = new ErrorData();
        errorData.setCode(400);
        errorData.setMessage(errorMessage);

        WebRtcSignalingMessage<ErrorData> error = new WebRtcSignalingMessage<>();
        error.setType(MessageType.ERROR);
        error.setData(errorData);
        send(session, error);
    }

    public int getConnectNum()
    {
        return sessionMap.size();
    }
}
