package com.server.config;

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
         * 쿠키가 크로스사이트 요청(Cross-Site Request)에서 전송될 수 있는지 여부를 결정
         *  - SameSite 쿠키 정책종류
         *    ㄴ> Strict : 쿠키를 설정한 동일한 도메인으로만 전송
         *    ㄴ> Lax    : 교차 사이트에서까지 쿠키 전송 ( 링크를 통한 접근 )
         *    ㄴ> None   : 모든 요청에 대해 쿠키를 전송함
         * */
        serializer.setSameSite("None");
        // HTTP 프로토콜일 경우에도 쿠키 전송 허용 [ 비권장 ]
        serializer.setUseSecureCookie(false);
        // 쿠키가 유효한 도메인을 지정
        serializer.setDomainName("localhost");
        return serializer;
    }
}
