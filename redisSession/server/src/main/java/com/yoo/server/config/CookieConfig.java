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

        // 1. 쿠키의 이름을 'unknowns'로 설정
        // 기본적으로 Spring Session은 'SESSION'이라는 이름을 사용하지만,
        // 여기서는 unknowns로 설정하여 테스트 진행
        // ㄴ> 개발자 도구 내 Cookie에 해당 unknown로 생성된것 확인 가능
        serializer.setCookieName("unknowns");

        // 2. 쿠키의 경로를 루트('/')로 설정합니다.
        // 이를 통해 해당 도메인 아래의 모든 경로에서 쿠키가 전송될 수 있도록 허용
        // --> 결과적으로 도메인이 다르면 접근이 불가능함
        //     ㄴ> Client 측 credentials: 'include' 설정으로 전송 필요
        serializer.setCookiePath("/");

        // 3. 도메인 이름을 정규 표현식으로 설정합니다.
        // 여기서는 정규식을 통해, '.example.com'처럼 상위 도메인과 하위 도메인을 모두 포함할 수 있도록 합니다.
        // 이 설정은 도메인이 적합한 경우 쿠키가 하위 도메인에서도 공유되도록 합니다.
        serializer.setDomainNamePattern("^.+?(\\w+\\.[a-z]+)$");

        return serializer;
    }

//    @Bean
//    public CookieSerializer cookieSerializer() {
//        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
//        /**
//         * 쿠키가 크로스사이트 요청(Cross-Site Request)에서 전송될 수 있는지 여부를 결정
//         *  - SameSite 쿠키 정책종류
//         *    ㄴ> Strict : 쿠키를 설정한 동일한 도메인으로만 전송
//         *    ㄴ> Lax    : 교차 사이트에서까지 쿠키 전송 ( 링크를 통한 접근 )
//         *    ㄴ> None   : 모든 요청에 대해 쿠키를 전송함
//         * */
//        serializer.setSameSite("None");
//        // HTTP 프로토콜일 경우에도 쿠키 전송 허용 [ 비권장 ]
//        serializer.setUseSecureCookie(false);
//        // 쿠키가 유효한 도메인을 지정
//        serializer.setDomainName("localhost");
//        return serializer;
//    }

}
