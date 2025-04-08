package com.mvtalker.utilities.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils
{
    private final GlobalJwtProperties jwtProperties;

    private final SecretKey key;

    @Autowired
    public JwtUtils(GlobalJwtProperties jwtProperties)
    {
        this.jwtProperties = jwtProperties;

        // 使用BASE64解码密钥字符串（推荐方式）
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Long parseJwt(String jwt)
    {
        try
        {
            Claims claims = Jwts.parserBuilder()
                    // 用签名（密钥）解析
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return Long.valueOf(claims.getSubject()); // 返回用户ID
        }
        catch (ExpiredJwtException e)
        {
            log.warn("JWT已过期: {}", e.getMessage());
        }
        catch (MalformedJwtException e)
        {
            log.warn("JWT格式错误: {}", e.getMessage());
        }
        catch (SignatureException e)
        {
            log.warn("JWT签名错误: {}", e.getMessage());
        }
        catch (Exception e)
        {
            log.error("解析JWT时出错: {}", e.getMessage());
        }
        return null;
    }

    public String generateJwt(Long userId)
    {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationTime()))
                // 设置签名（密钥）
                .signWith(key)
                // 合并信息
                .compact();
    }
}
