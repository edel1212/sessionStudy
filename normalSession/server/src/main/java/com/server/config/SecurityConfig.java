package com.server.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.formLogin(custom->{
            custom.loginProcessingUrl("/login");
        });

        // 모든 접근 제한
        http.authorizeHttpRequests( access ->{
            access.anyRequest().authenticated();
        });
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 인메모리 사용자 저장소. 실제 서비스에서는 DB를 사용할 것을 권장
        User.UserBuilder users = User.withDefaultPasswordEncoder();
        return new InMemoryUserDetailsManager(
                users.username("user").password("123").roles("USER").build(),
                users.username("admin").password("123").roles("ADMIN").build()
        );
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring()
                .requestMatchers(HttpMethod.POST,"/login")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }


}
