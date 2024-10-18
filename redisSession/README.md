# NextJs(Client) <-> SpringBoots(Servers)   [width Redis Session]

```properties
# ✅ Redis Session Clustering를 사용해서 MAS 구조에서도 Session을 공유

# 🗨 분선 서버 구조에서 SessionID 전달 방식
#  ◽ 방법 1 -  Cookie에 Session ID 전달
#  ◽ 방법 2 -  Custom Header에 Session ID 전달
```

## Session ID 전달 방식 비교

| **방식**                     | **장점**                                                                                      | **단점**                                                   |
|------------------------------|-----------------------------------------------------------------------------------------------|----------------------------------------------------------|
| **Cookie에 Session ID**         | - 브라우저가 자동으로 관리 (쿠키 자동 전송)                                                   | - CSRF 공격에 취약 (추가적으로 CSRF 토큰 필요)                         |
|                              | - `SameSite`, `HttpOnly`, `Secure` 속성으로 추가 보안 설정 가능                                | - 브라우저 제약이나 CORS 설정에 의해 쿠키 전송이 차단될 수 있음                  |
|                              | - 별도의 클라이언트 로직 없이 브라우저가 처리                                                 | - 비 브라우저 환경에서 사용하기 어려움 ( **장점이 될 수 있음!** )               |
| **Custom Header에 Session ID**| - CSRF 공격 방어 가능                                       | - 클라이언트에서 모든 요청마다 Session ID를 설정해야 함     |
|                              | - 브라우저 외의 환경(모바일 앱, 서버 간 통신 등)에서도 쉽게 사용 가능                           | - 쿠키에 비해 구현 복잡도 증가 (클라이언트 코드에서 관리 필요)                    |
|                              | - 쿠키 관련 브라우저 정책에 구애받지 않음 (SameSite, CORS 문제 없음)                          | - 헤더에 Session ID가 담길 경우, HTTPS 미사용 시 보안에 취약  |


## 흐름

![alt text](image.png)


## 공통 Server 설정

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

## Cookie 전달 방식 Server 설정

- ### Cookie 설정
  - 주의사항
    - Redis Session 사용 시 기본 설정 Session Key값이 `SESSION`으로 변경됨에따라 추가적인 변경 설정이 필요
      - `serializer.setCookieName("JSESSIONID");`
    - 🛑 **삽질**`setSameSite` 설정 주의 `None`으로 설정할 경우 Session ID 값을 Client -> Server 전달 받지 못함
      - Chrome의 경우 보안을 위해  자체적으로 막음
      - `None`을 사용하고 싶다면 `setUseSecureCookie(true)`를 사용해야 함
      
### SameSite 속성 요약

- **SameSite=Lax**:
  - 동일한 사이트 내에서만 쿠키가 전송되며, 외부 링크에서 넘어온 경우에도 **GET 요청**에 한해서만 쿠키가 전송
  - **기본 설정**이며, 보안과 사용 편의성 간의 균형을 유지
  - **예시**: 사용자가 사이트 A에서 링크를 클릭하여 사이트 B로 이동하는 경우, 사이트 B에서의 **GET 요청** 시 쿠키가 전송됩니다. 하지만 사이트 A에서의 **POST 요청** 시 **쿠키 전송X**

- **SameSite=Strict**:
  - **다른 도메인**에서의 모든 요청에 대해 **쿠키가 전송되지 않음**
  - 외부 링크나 POST 요청이 있을 때 쿠키가 차단될 수 있어 엄격한 보안을 제공
  - **예시**: 사용자가 사이트 A에서 링크를 클릭하여 사이트 B로 이동하면, 사이트 B에서의 **모든 요청**(GET, POST 등)에 대해 **쿠키 전송X**

- **SameSite=None**:
  - 모든 도메인의 요청에 대해 쿠키가 전송되지만 보안에 굉장히 취약
  - 크로스 사이트 요청에 대해 쿠키가 필요할 때 사용

```java
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
```
