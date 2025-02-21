package com.mvtalker.gateway.utilities;

import com.mvtalker.gateway.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil
{
    @Value("${jwt.expiration-time}")
    private long expirationTime; // 以毫秒为单位

    @Value("${jwt.secret}")
    private String jwtSecret;
    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration-time}") long expirationTime) {
        this.jwtSecret = jwtSecret;
        this.expirationTime = expirationTime;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String parseJwt(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims.getSubject(); // 返回用户ID
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT已过期", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("无效的JWT令牌", e);
        } catch (SignatureException e) {
            throw new JwtException("无效的JWT签名", e);
        } catch (Exception e) {
            throw new JwtException("解析JWT时出错", e);
        }
    }

    public String generateJwt(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }
}
