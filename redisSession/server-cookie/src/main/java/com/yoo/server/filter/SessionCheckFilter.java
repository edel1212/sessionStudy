package com.yoo.server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class SessionCheckFilter extends OncePerRequestFilter {

    private final RedisSessionRepository redisSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Into the Session Chain");

        HttpSession session = request.getSession();
        log.info("Session ID: {}", session.getId());

        // 현재 세션에서 SecurityContext 확인
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();

            if (authentication != null) {
                String username = authentication.getName();

                log.info("User attempting to login: {}", username);

                // Redis에서 로그인 정보 찾기
                // "loginUserData:" + username 키에서 "sessionId"와 "isLoggedIn" 필드를 각각 조회
                String sessionId = (String) redisTemplate.opsForHash().get("loginUserData:" + username, "sessionId");
                String isLoggedIn = (String) redisTemplate.opsForHash().get("loginUserData:" + username, "isLoggedIn");

                log.info("Session ID: " + sessionId);
                log.info("Is Logged In: " + isLoggedIn);

            }
        }

        filterChain.doFilter(request, response);
    }

}
