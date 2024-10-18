package com.yoo.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        /**
         * 😔 보안 문제로 생긴 이슈 [ 삽질 3일.. ]
         *
         *  원인 : Redis 세션과 기본 세션 간의 전송 방식의 차이로 인해 Secure 속성을 설정하지 않은 경우 쿠키가 자동으로 전달이 막힘
         *      - Redis 세션은 기본 세션과는 달리 외부 저장소에서 세션 데이터를 관리함 따라서 안전하지 않은 설정을 통해 쿠키를
         *      사용 세션 ID의 전송할 경우 브라우저가 이를 차단 --> 서버가 Redis에서 세션을 조회할 수 없게 됨
         *
         * // serializer.setSameSite("None");
         *
         * */
        // HTTP 프로토콜일 경우에도 쿠키 전송 허용 [ 비권장 ]
        serializer.setUseSecureCookie(false);
        // 쿠키가 유효한 도메인을 지정
        serializer.setDomainName("localhost");
        return serializer;
    }
}

