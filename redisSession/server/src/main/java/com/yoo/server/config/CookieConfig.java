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
        // 기본 세션 저장소를 사용 시 세션 ID가 JSESSIONID라는 이름으로 클라이언트의 쿠키에 저장하나, Spring Session과 Redis를 사용하게 되면,
        // Spring Session이 세션 관리를 전담하게 되므로 SESSION이라는 기본 쿠키 이름으로 변경된다 따라서 재정이를 해줘야함
        serializer.setCookieName("JSESSIONID");
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
