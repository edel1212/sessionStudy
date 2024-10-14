# NextJs(Client) <-> SpringBoots(Servers)   [width Redis Session]

```properties
✅ Redis Session Clustering를 사용해서 병렬적 서버 구조에서도 Session을 공유
```

## 흐름

![alt text](image.png)


## Server 설정

- ### Dependencies 추가
```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-redis'
	implementation 'org.springframework.session:spring-session-data-redis'
}
```

- ### application 설정
  - `namespace`를 사용해서 Redis에 저장될 Session Key 값 지정
  - `store-type` 지정을 통해 Session을 Redis에 저장할 것을 알림
```properties
spring:
  session:
    store-type: redis
    redis:
      # Redis에 저장될 Key Prefix
      namespace: yoo:session

  ############################
  ## Redis Setting
  # docker run -d --name security-redies-db -p 6379:6379 redis --requirepass "123"
  ############################
  data:
    redis:
      host: localhost
      port: 6379
      password: 123
```

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
