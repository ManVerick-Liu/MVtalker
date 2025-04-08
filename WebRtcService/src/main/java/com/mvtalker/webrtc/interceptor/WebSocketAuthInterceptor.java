package com.mvtalker.webrtc.interceptor;

import com.mvtalker.utilities.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.List;

/**
 * HandlerInterceptor仅拦截HTTP请求，而WebSocket握手在协议升级（HTTP -> WS）后不再走Servlet容器流程。
 * WebSocket握手需要通过HandshakeInterceptor实现拦截
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor extends ServerEndpointConfig.Configurator
{
    private static JwtUtils jwtUtils; // 注意这里是静态变量
    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        WebSocketAuthInterceptor.jwtUtils = jwtUtils;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec,
                                HandshakeRequest request,
                                HandshakeResponse response)
    {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty())
        {
            String token = headers.get(0).replace("Bearer ", "");
            try
            {
                Long userId = jwtUtils.parseJwt(token);
                sec.getUserProperties().put("userId", userId);
            }
            catch (Exception e)
            {
                response.getHeaders().put("Connection",
                        Collections.singletonList("close"));
            }
        }
    }
}
