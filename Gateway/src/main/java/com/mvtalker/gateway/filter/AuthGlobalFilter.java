package com.mvtalker.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvtalker.utilities.common.GlobalConstantValue;
import com.mvtalker.utilities.entity.baseResponse.BaseResponse;
import com.mvtalker.gateway.config.LocalJwtProperties;
import com.mvtalker.utilities.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

// 实现Ordered是为了确保本过滤器的优先级高于NettyRoutingFilter，因为NettyRoutingFilter会对请求进行转发
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered
{
    private final LocalJwtProperties localJwtProperties;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        try
        {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            if (shouldExcludePath(path))
            {
                log.debug("Bypassing auth for excluded path: {}", path);
                return chain.filter(exchange);
            }

            return processAuthentication(exchange, chain);
        } catch (Exception e) {
            log.error("Unexpected gateway error: {}", e.getMessage(), e);
            return buildErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "系统异常", "网关服务内部错误");
        }
    }

    private Mono<Void> processAuthentication(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 跳过已建立WebSocket连接的后续请求
        if (isWebSocketConnected(exchange))
        {
            return chain.filter(exchange);
        }

        // 1. 验证Authorization头格式
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            log.warn("缺失或无效的认证头 | Path: {}", request.getPath());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "认证头错误", "Authorization头必须以Bearer开头");
        }

        // 2. 提取并验证JWT
        String jwt = authHeader.substring(7);
        if (jwt.isEmpty())
        {
            log.warn("空令牌 | Path: {}", request.getPath());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "无效令牌", "令牌内容不能为空");
        }

        // 3. 解析JWT
        Long userId = jwtUtils.parseJwt(jwt);
        if (userId == null)
        {
            log.warn("JWT解析失败 | Token: {} | Path: {}", jwt, request.getPath());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                    "令牌验证失败", "无效的访问令牌");
        }

        log.debug("认证成功 | 用户ID: {} | Path: {}", userId, request.getPath());

        // 4. 传递用户信息到下游服务
        ServerHttpRequest newRequest = request.mutate()
                .header(GlobalConstantValue.USER_CONTEXT_ID_HEADER_NAME, userId.toString())
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());

    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String errorType, String message)
    {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        BaseResponse<Void> responseBody = new BaseResponse<>();
        responseBody.setCode(status.value());
        responseBody.setMessage(message);
        responseBody.setTimestamp(LocalDateTime.now());

        try
        {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        }
        catch (JsonProcessingException e)
        {
            log.error("响应序列化失败: {}", e.getMessage());
            return response.setComplete();
        }
    }

    private boolean shouldExcludePath(String path)
    {
        log.debug("当前路径: {} | 排除列表: {}", path, localJwtProperties.getExcludePaths());
        // AntPathMatcher是专门用于匹配/user/**这种风格的路径的
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String excludePath : localJwtProperties.getExcludePaths())
        {
            if (pathMatcher.match(excludePath, path))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isWebSocketConnected(ServerWebExchange exchange)
    {
        return "WebSocket".equalsIgnoreCase(
                exchange.getRequest().getHeaders().getFirst("Upgrade"))
                && exchange.getResponse().getStatusCode() == HttpStatus.SWITCHING_PROTOCOLS;
    }

    @Override
    public int getOrder()
    {
        // 优先级比NettyRoutingFilter高
        return 0;
    }
}
