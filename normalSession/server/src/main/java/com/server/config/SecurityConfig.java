package com.server.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ℹ️ CSRF 미사용
        http.csrf(csrf -> csrf.disable());

        // ℹ️ Form 설정
        http.formLogin(formLogin -> {
            formLogin.loginProcessingUrl("/login");
        });

        // 접근 제한 추가
        http.authorizeHttpRequests(access -> {
            access.requestMatchers("/login").permitAll();
            access.anyRequest().authenticated();
        });

        return http.build();
    }
}
