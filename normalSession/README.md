# NextJs(Client) <-> SpringBoot(Server)

```properties
# ℹ️ Monolithic Architecturer가 아닌 Server 와 UI가 나눠진 형태에서의 Session 적용
#  ㄴ Spring Security, CSRF 적용하여 인가 및 인증 테스트
```

## CSRF Token(Cross-Site Request Forgery Token)

```properties
# ℹ SpringSecurity에서 GET-Method는 CRSF Token을 검증하지 않음
#  ㄴ> 삽질 오래함..
```

- ### CSRF 공격이란?
  - 사용자가 로그인한 상태에서 악성 웹사이트를 방문했을 때, 사용자의 권한으로 웹 애플리케이션에 요청을 보내는 것
    - 로그인한 세션을 이용해 권한을 악용하는 공격
- ### CSRF 토큰의 원리
  - `Server`요청에 대해 임의의 고유한 **토큰**을 생성 후 `Client`에 전달
  - `Client`가 `Server`에 다른 요청 시 `CSRF Token`을 포함하도록 요구

## Server 설정

- ### Method 수준 접근 제어 설정

  - Security 설정 클래스 내 `@EnableMethodSecurity` 선언
  - 주의 사항🤩

    - `authorizeHttpRequests(req->req.anyRequest~)`와 같은 체이닝 접근 제어 사용 금지
    - 해당 설정이 Method수준의 접근 제어보다 **우선 적용**되기 때문

    ```java
    @EnableWebSecurity
    @EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            /**
            * 😱 @EnableMethodSecurity 사용할 경우 해당 코드 사용 금지!
            * - 접근 제어 중복으로 인해 원치 않은 접근 제한이 된다.
            http.authorizeHttpRequests(access -> {
                access.anyRequest().authenticated();
            });
            **/
            return http.build();
        }
    }
    ```

- ### CORS 설정

  - `Client` 와 `Server`의 도메인이 다르기 때문
  - `addAllowedOriginPattern`을 사용하면 와일드 카드 `("*")` 사용 가능
  - 코드

    ```java
    @Component
    @EnableWebSecurity
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.cors(cors->{
                cors.configurationSource(corsConfigurationSource());
            });

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

    ```

- ### Session 설정
  - `SessionCreationPolicy`를 사용하여 세션 생성 방식 설정
  - 종류
    - `ALWAYS` : 매 요청 시 세션을 생성함
    - `IF_REQUIRED` : 기본 설정. 필요할 때만 생성, 세션이 이미 존재하면 이를 재사용
    - `NEVER` : 기존에 존재하면 사용하나, 그렇지 않으면 생성 조차 하지 않음
    - `STATELESS` : 세션을 전혀 사용하지 않음
  ```java
  @EnableWebSecurity
  public class SecurityConfig {
      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
          // 세션 설정
          http.sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
          return http.build();
      }
  }
  ```
- ### Cookie 설정

  - `Server`가 `Client`에 `Cookie`를 제공 후 어떤 조건에서 `Server`로 다시 전송될 수 있는지를 설정

  ```java
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
  ```

## Log-in

- ### Server

  - `CSRF Token`검증 로직은 `Security Filter` 내에서 자체 **자동 검증**
  - 😱 삽질
    - `SecurityContextHolder`내에 인증이 완려된 `Authentication` 객체 주입 필요
    - 해당 `SecurityContextHolder`객체를 `Session`을 생성 후 주입 함
      ```properties
      ### 새로운 Session 생성
      HttpSession session = request.getSession(true);
      ### Session 주입
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
      ```
  - 😱 삽질2

    - `Session`에 `SecurityContextHolder`주입할 시 Key값을 **동적**으로 구성 시 확인 **불가능**

      - 👍 Key값을 `Type Safety`하게 `HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY`를 사용해주자

    - Controller

    ```java
    @RestController
    @RequiredArgsConstructor
    public class LoginController {
        // Spring Security Manager
        private final AuthenticationManagerBuilder authenticationManagerBuilder;

        @PreAuthorize("isAnonymous()")
        @PostMapping("/login")
        public ResponseEntity<Map<String, String>> login(String username, String password, HttpServletRequest request) {
            // 1. 사용자의 Request를 통해 Id, 비밀번호를 받아 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            // 2. 해당 토큰값으로 검증 실행 -> UserDetailsService 구현체를 통해 DB 검증
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            // 3. 인증 정보를 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 4. 새로운 세션 생성
            HttpSession session = request.getSession(true);

            // ℹ️ 각각의 사용자의 HttpSession session 정보는 다르기에 Key 중복은 일어나지 않는다.
            // 😱 삽질 3시간. . 단 해당 Key 값을 동적으로 할당할 경우 Session을 찾지 못하는 이슈가 있다..
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            Map<String, String> result = new HashMap<>();
            result.put("userName", authentication.getName());

            return ResponseEntity.ok(result);
        }
    }

    /*********************************************************************************************************/

    @RestController
    public class CsrfController {
        @GetMapping("/csrf")
        public Map<String, String> getCsrfToken(HttpServletRequest request) {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", csrfToken.getToken());
            return tokenMap;
        }
    }
    ```

    - Service

    ```java
    @Service
    @RequiredArgsConstructor
    public class LoginServiceImpl implements UserDetailsService {

        private final PasswordEncoder passwordEncoder;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            String encodedPassword = passwordEncoder.encode("123");
            // 현재 로직으로는 하드코딩 비밀번호, 권한을 넣음
            // 실제 로직은 DB내 값을 넣고 해당 객체에 주입되는 값과
            // Controller에서 주입했던 UsernamePasswordAuthenticationToken()에 주입 비밀번호와
            // 비교 로직을 Security가 내부 로직으로 실행함
            return new User(username,encodedPassword, this.authorities(Set.of("Admin")));
        }

        private Collection<? extends GrantedAuthority> authorities(Set<String> roles){
            return roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_"+r))
                    .collect(Collectors.toSet());
        }
    }
    ```

- ### Client

  - CSRF Token
    - 해당 프로젝트는 `useState()`로 토큰을 관리 하지만 `Recoil`적용을 적용 하는 방법도 있음
    - 실제 프로젝트 적용 시 `SSR` 방식으로 불러와 처리
    - 전송 방식
    - Form형태
      - `_csrf`를 Key 로 전송 - `formData.append("_csrf", csrfToken);`
    - Header 형태
      - `X-CSRF-TOKEN`를 Key로 전송 - `"X-CSRF-TOKEN": csrfToken`
  - 중요 포인트🤩
    - 요청 내 `credentials: "include"`를 사용하여 `Cookie`값을 함께 보내주자
  - 코드

  ```javascript
  const [csrfToken, setCsrfToken] = useState("");

  // Get CSRF Token
  const fetchCsrfToken = async () => {
    const response = await fetch("http://localhost:8080/csrf", {
      credentials: "include",
    });
    const data = await response.json();
    setCsrfToken(data.token); // 서버에서 받은 CSRF 토큰 설정
  };

  // 🎶 컴포넌트가 마운트될 때 CSRF 토큰을 가져옴
  useEffect(() => {
    fetchCsrfToken();
  }, []);

  const logIn = async () => {
    const formData = new URLSearchParams();
    formData.append("username", "yoo");
    formData.append("password", "123");
    formData.append("_csrf", csrfToken);

    const response = await fetch("http://localhost:8080/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString(),
      // ℹ️ 해당 설정을 통해 Session 정보값을 쿠키에 받음
      credentials: "include",
    });
    const data = await response.json();
    console.log(data);
    if (response.ok) {
      console.log("로그인 성공:", data);
    } else {
      console.error("로그인 실패");
    }
  };
  ```

## Log-out

- ### Server

  - 주의사항
    - `POST`방식의 `/logout` URL을 사용하려면 따로 Security 설정을 해줘야 함
      - **Default Spring Security 설정** 값 이기에 요청 시 **원하지 않는** 로직으로 **실행**
    - Logout으로 사용 중인 Session을 삭제 했다면 기존에 사용 중인 `CSRF Token` **재발급** 받아줘야 함
      - 이전 사용 중인 `CSRF`는 **세션**이 **삭제** 되면서 **사용할 수 없기 때문**

  ```java
  @Log4j2
  @RestController
  @RequiredArgsConstructor
  public class MemberController {
      @PostMapping("/member/logout")
      public ResponseEntity<String> logout(HttpServletRequest request, Authentication authentication) {
          // 현재 세션을 가져옵니다.
          HttpSession session = request.getSession(false); // false로 설정하면 세션이 없을 때 새로운 세션을 만들지 않습니다.

          log.info("-- Session 삭제 진입--");
          if (session != null) {
              log.info("-- Session 삭제 성공--");
              session.invalidate(); // 세션 무효화
          }

          // 로그아웃 성공 메시지 반환
          return ResponseEntity.ok("Logged out successfully");
      }
  }
  ```

- ### Client

  - 주의 사항
    - 세션 삭제 후 재 로그인이 필요할 경우 `CSRF Token` 재발급

  ```javascrip
    const logOut = async () => {
    fetch("http://localhost:8080/member/logout", {
      method: "POST",
      headers: {
        "X-CSRF-TOKEN": csrfToken, // 헤더에 CSRF 토큰 포함
      },
      credentials: "include", // 쿠키/세션 정보 포함
    }).then((response) => {
      if (response.ok) {
        alert("로그아웃 성공!");
        console.log("Logged out successfully");
        setResponseData("Logged out successfully"); // 로그아웃 결과 출력
        // ✨ Log - Out  시 CRSF 토큰도 같이 날라가므로 새로 초기화 해주자
        fetchCsrfToken();
      }
    });
  };
  ```

## 접근 제어

- ### Client

  - 로그인 후 `Cookie`에 저장되는 `Session`을 API 요청 시 **전달만** 해주면 된다.
    - 요청 내 `credentials: "include"`를 추가

  ```javascript
  "use client";
  import { useEffect, useState } from "react";

  export default function NormalSession() {
    const [csrfToken, setCsrfToken] = useState("");
    const [responseData, setResponseData] = useState("");

    // 컴포넌트가 처음 렌더링될 때 CSRF 토큰을 가져오는 함수
    const fetchCsrfToken = async () => {
      const response = await fetch("http://localhost:8080/csrf", {
        credentials: "include", // 쿠키 포함
      });
      const data = await response.json();
      setCsrfToken(data.token);
      // JSON 데이터를 문자열로 변환하여 상태에 저장
      setResponseData(JSON.stringify(data, null, 2));
    };

    useEffect(() => {
      fetchCsrfToken(); // 컴포넌트가 마운트될 때 CSRF 토큰을 가져옴
    }, []);

    const apiResponse = async (url: string) => {
      const response = await fetch(url, {
        method: "POST",
        headers: {
          // 헤더에 CSRF 토큰 포함
          "X-CSRF-TOKEN": csrfToken,
        },
        credentials: "include",
      });
      const data = await response.json();
      if (response.ok) {
        setResponseData(JSON.stringify(data, null, 2));
      } else {
        console.error("요청 실패");
        setResponseData("Error: 요청에 실패했습니다.");
      }
    };

    return <></>;
  }
  ```

- ### Server

  - Spring Security 내 Filter를 통해 자동으로 권한별 접근 제어 처리를 해준다.

  ```java
  @Log4j2
  @RestController
  public class AuthCheckController {

      @PostMapping("/all")
      @PreAuthorize("permitAll()")
      public ResponseEntity<Map<String, String>> all(){
          Map<String, String> msg = new HashMap<>();
          msg.put("msg", "All Access");
          return ResponseEntity.ok(msg);
      }

      // 인증되지 않은 사용자
      @PostMapping("/no-login")
      @PreAuthorize("isAnonymous()")
      public ResponseEntity<Map<String, String>> noLogin(){
          Map<String, String> msg = new HashMap<>();
          msg.put("msg", "Doesn't have Auth");
          return ResponseEntity.ok(msg);
      }

      // 인증된 사용자
      @PostMapping("/has-certified")
      @PreAuthorize("isAuthenticated()")
      public ResponseEntity<Map<String, String>> isAuthenticated(@AuthenticationPrincipal UserDetails userDetails) {
          String authorities = userDetails.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("권한 없음");

          // 사용자 정보와 권한을 Map에 담기
          Map<String, String> userInfo = new HashMap<>();
          userInfo.put("username", userDetails.getUsername());
          userInfo.put("password", userDetails.getPassword());
          userInfo.put("authorities", authorities);

          // Map을 JSON 응답으로 반환
          return ResponseEntity.ok(userInfo);
      }

      @PostMapping("/admin")
      @PreAuthorize("hasRole('Admin')")
      public ResponseEntity<Map<String, String>> admin(@AuthenticationPrincipal UserDetails userDetails) {
          String authorities = userDetails.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("권한 없음");
          // 사용자 정보와 권한을 Map에 담기
          Map<String, String> userInfo = new HashMap<>();
          userInfo.put("username", userDetails.getUsername());
          userInfo.put("password", userDetails.getPassword());
          userInfo.put("authorities", authorities);
          // Map을 JSON 응답으로 반환
          return ResponseEntity.ok(userInfo);
      }
  }
  ```
