package com.server.config;

import com.server.handler.CustomAccessDeniedHandler;
import com.server.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Component
@Log4j2
@EnableWebSecurity
@RequiredArgsConstructor
// 메서드 수준의 보안 설정을 활성화
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    // 접근 제어 핸들러
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ℹ️ CSRF 미사용
        http.csrf(csrf -> csrf.disable());
        // ℹ️ CORS 설정
        http.cors(cors->{
            cors.configurationSource(corsConfigurationSource());
        });

        http.exceptionHandling(handling ->
                handling
                        // ✨ 인증된 사용자가 권한이 없을 때 호출
                        .accessDeniedHandler(customAccessDeniedHandler)
                        // ✨ 인증되지 않은 사용자가 보호된 리소스에 접근
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
        );

       /**
        * 😱 @EnableMethodSecurity 사용할 경우 해당 코드 사용 금지!
        * - 접근 제어 중복으로 인해 원치 않은 접근 제한이 된다.
        http.authorizeHttpRequests(access -> {
            access.anyRequest().authenticated();
        });
        **/

        return http.build();
    }

    /**
     * <h3>CORS 설정</h3>
     *
     * @return the cors configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 새로운 CORS 설정 객체 생성
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 출처에서의 요청을 허용
        configuration.addAllowedOriginPattern("*");
        // 모든 HTTP 메소드를 허용 (GET, POST, PUT, DELETE, OPTIONS 등)
        configuration.setAllowedMethods(Collections.singletonList("*"));
        // 모든 HTTP 헤더를 허용
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 자격 증명(예: 쿠키, 인증 정보)을 포함한 요청을 허용
        configuration.setAllowCredentials(true);
        // 캐시 시간을 3600초(1시간)으로 설정
        configuration.setMaxAge(3600L);

        // URL 경로에 기반한 CORS 설정 소스 객체 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위에서 설정한 CORS 구성을 등록
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
