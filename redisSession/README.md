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
