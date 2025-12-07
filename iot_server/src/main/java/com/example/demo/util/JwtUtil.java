package com.example.demo.util;

import com.example.demo.exception.TokenExpiredException;
import com.example.demo.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成Token（包含角色信息）
     */
    public String generateToken(Long userId, String phone, String platform, String roleId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("phone", phone)
                .claim("platform", platform)  // 平台类型：mobile/web
                .claim("roleId", roleId)      // 角色ID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token 已过期，请重新登录");
        } catch (Exception e) {
            throw new TokenInvalidException("Token 无效");
        }
    }

    /**
     * 从Token中获取手机号
     */
    public String getPhoneFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return (String) claims.get("phone");
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token 已过期，请重新登录");
        } catch (Exception e) {
            throw new TokenInvalidException("Token 无效");
        }
    }

    /**
     * 从 Token 中获取平台类型
     */
    public String getPlatformFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return (String) claims.get("platform");
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token 已过期，请重新登录");
        } catch (Exception e) {
            throw new TokenInvalidException("Token 无效");
        }
    }

    /**
     * 从 Token 中获取角色ID
     */
    public String getRoleIdFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return (String) claims.get("roleId");
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token 已过期，请重新登录");
        } catch (Exception e) {
            throw new TokenInvalidException("Token 无效");
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

