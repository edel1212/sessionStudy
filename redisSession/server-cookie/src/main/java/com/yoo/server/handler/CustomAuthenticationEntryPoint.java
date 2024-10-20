package com.yoo.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error(authException.getMessage());
        log.info("- Custom Authentication Entry PointHandler 접근 -");
        var objectMapper = new ObjectMapper();
        int scUnauthorized = HttpServletResponse.SC_UNAUTHORIZED;
        response.setStatus(scUnauthorized);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, String> msg = new HashMap<>();
        msg.put("msg","인증되지 않은 사용자가 보호된 리소스에 접근");
        response.getWriter().write(objectMapper.writeValueAsString(msg));
    }
}
