package com.mvtalker.gateway.filter;

import com.mvtalker.gateway.exception.JwtException;
import com.mvtalker.gateway.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// 实现Ordered是为了确保本过滤器的优先级高于NettyRoutingFilter，因为NettyRoutingFilter会对请求进行转发
@Component
public class AuthGlobalFilter implements GatewayFilter, Ordered
{

    @Value("${jwt.exclude-paths}")
    private List<String> excludePaths;

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        // 获取请求
        ServerHttpRequest request = exchange.getRequest();
        // 获取响应
        ServerHttpResponse response = exchange.getResponse();

        // 获取请求路径
        String path = request.getPath().value();

        // 检查路径是否在排除列表中
        if (shouldExcludePath(path)) {
            // 如果路径在排除列表中，直接放行
            return chain.filter(exchange);
        }


        // 校验请求头Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 如果没有找到Authorization头或者不是Bearer开头，返回401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 校验JWT
        String jwt = authHeader.substring(7);

        String userId = null;

        try {
            // 使用JwtUtil解析JWT并获取用户ID
            userId = jwtUtil.parseJwt(jwt);

        } catch (JwtException e) {
            // 如果JWT无效，返回401并设置错误信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 网关向微服务传递用户信息, 将用户ID添加到请求头中
        String userIdHeader = userId;
        ServerWebExchange newExchange = exchange.mutate().request(builder -> builder.header("userInfo", userIdHeader)).build();

        return chain.filter(newExchange);
    }

    private boolean shouldExcludePath(String path)
    {
        // AntPathMatcher是专门用于匹配/user/**这种风格的路径的
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String excludePath : excludePaths)
        {
            if (pathMatcher.match(excludePath, path))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder()
    {
        // 优先级比NettyRoutingFilter高
        return 0;
    }
}
