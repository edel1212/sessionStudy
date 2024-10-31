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
import java.util.Objects;

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
        log.info("Current Session ID: {}", session.getId());

        // 현재 세션에서 SecurityContext 확인
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

        if (securityContext != null) {
            Authentication authentication = securityContext.getAuthentication();

            if (authentication != null) {
                String username = authentication.getName();
                log.info("User attempting to login: {}", username);

                // Redis에서 기존 세션 ID와 로그인 상태 조회
                String redisSessionIdKey = "loginUserData:" + username;
                String previousSessionId = (String) redisTemplate.opsForHash().get(redisSessionIdKey, "sessionId");
                String isLoggedIn = (String) redisTemplate.opsForHash().get(redisSessionIdKey, "isLoggedIn");

                log.info("Previous Session ID from Redis: " + previousSessionId);
                log.info("Is Logged In (from Redis): " + isLoggedIn);

                // Redis에 저장된 세션 ID가 현재 세션과 다르고, 이미 로그인된 상태라면 기존 세션을 삭제
                if (!Objects.equals(session.getId(), previousSessionId) && "true".equals(isLoggedIn)) {
                    if (previousSessionId != null) {
                        log.info("Deleting previous session ID: {}", previousSessionId);
                        redisSessionRepository.deleteById(previousSessionId); // 기존 세션 삭제
                    }
                    // Redis에 새로운 세션 정보 업데이트
                    redisTemplate.opsForHash().put(redisSessionIdKey, "sessionId", session.getId());
                    redisTemplate.opsForHash().put(redisSessionIdKey, "isLoggedIn", "true");
                    log.info("Updated Redis with new session ID: {}", session.getId());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}
