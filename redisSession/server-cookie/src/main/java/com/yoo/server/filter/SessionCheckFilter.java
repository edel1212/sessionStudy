package com.yoo.server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.User;
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

//                // Redis에서 현재 사용자의 세션을 찾습니다.
//                RedisSession loginUserInfo =  redisSessionRepository.findById(username);
//                if (!redisSession.getId().equals(session.getId())) {
//                    // 다른 세션이 존재할 경우 해당 세션을 삭제 (중복 로그인 방지)
//                    log.info("Existing session found for user: {}. Invalidating session ID: {}", username, redisSession.getId());
//                    redisSessionRepository.deleteById(redisSession.getId());
//                }
            }
        }

        filterChain.doFilter(request, response);
    }

}
