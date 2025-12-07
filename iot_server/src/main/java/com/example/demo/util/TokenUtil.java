package com.example.demo.util;

import com.example.demo.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final JwtUtil jwtUtil;

    /**
     * 从token中获取用户ID
     * @param token JWT token
     * @return 用户ID
     * @throws TokenInvalidException 如果token无效
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenInvalidException("请先登录");
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}

