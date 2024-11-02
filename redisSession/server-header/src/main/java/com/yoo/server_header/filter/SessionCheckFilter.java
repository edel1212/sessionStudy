package com.yoo.server_header.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
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
        // 1. Get Header 값
        String sessionId = request.getHeader("x-auth-token");

        // Header 값이 없을 경우 Skip
        if(sessionId == null){
            filterChain.doFilter(request, response);
            return;
        }// if

        // 2. sessionId을 사용 Redis에서 Session 존재 여부 확인
        Session session = redisSessionRepository.findById(sessionId);

        // 3. Check if session exists
        if (session != null) {
            
            // Log ::  Session found in Redis: 5844cdce-cdb0-4f6c-9640-ac865eaafda4
            log.info("Session found in Redis: " + sessionId);


            // 3-1. Session 내 SecurityContext 값 추출
            SecurityContext securityContext = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

            //  Check SecurityContext exists
            if (securityContext != null) {
                
                // Log :: Authenticated user: yoo
                log.info("Authenticated user: " + securityContext.getAuthentication().getName());
                
                // ✅ SecurityContextHolder에 값을 주입 - 가장 중요 로직
                SecurityContextHolder.setContext(securityContext);
                
            }  else {
                log.warn("No SecurityContext found in Redis session.");
            } // if - else
            
        } else {
            log.warn("No session found in Redis for sessionId: " + sessionId);
        } // if

        filterChain.doFilter(request, response);
    }
}
