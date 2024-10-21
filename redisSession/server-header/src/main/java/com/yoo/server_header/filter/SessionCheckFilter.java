package com.yoo.server_header.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContext;
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
        String sessionId = request.getHeader("x-auth-token");
        log.info("--------------");
        log.info("x-auth-token :: " + sessionId );
        log.info("--------------");
        if(sessionId == null){
            filterChain.doFilter(request, response);
            return;
        }// if

        // 1. Get the session from Redis
        Session session = redisSessionRepository.findById(sessionId);

        // 2. Check if session exists
        if (session != null) {
            // Log ::  Session found in Redis: 5844cdce-cdb0-4f6c-9640-ac865eaafda4
            log.info("Session found in Redis: " + sessionId);
            // Get Security Context on Redis session
            SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (securityContext != null) {
                // Log :: Authenticated user: yoo
                log.info("Authenticated user: " + securityContext.getAuthentication().getName());
            } // if
        } else {
            log.warn("No session found in Redis for sessionId: " + sessionId);
        } // if

        filterChain.doFilter(request, response);
    }
}
