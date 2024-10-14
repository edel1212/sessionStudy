- ### Cookie 설정
  - 주의사항
    - Redis Session 사용 시 기본 설정 Session Key값이 `SESSION`으로 변경됨에따라 추가적인 변경 설정이 필요
      - `serializer.setCookieName("JSESSIONID");`
```java
@Configuration
public class CookieConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 기본 세션 저장소를 사용 시 세션 ID가 JSESSIONID라는 이름으로 클라이언트의 쿠키에 저장하나, Spring Session과 Redis를 사용하게 되면,
        // Spring Session이 세션 관리를 전담하게 되므로 SESSION이라는 기본 쿠키 이름으로 변경된다 따라서 재정이를 해줘야함
        serializer.setCookieName("JSESSIONID");
        // 쿠키 CORS 설정
        serializer.setSameSite("None");
        // HTTP 프로토콜일 경우에도 쿠키 전송 허용 [ 비권장 ]
        serializer.setUseSecureCookie(false);
        // 쿠키가 유효한 도메인을 지정
        serializer.setDomainName("localhost");
        return serializer;
    }

}
```
