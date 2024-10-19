"use client";

import { useEffect, useState } from "react";

export default function Home() {
  const [csrfToken, setCsrfToken] = useState("");

  // 컴포넌트가 처음 렌더링될 때 CSRF 토큰을 가져오는 함수
  const fetchCsrfToken = async () => {
    const response = await fetch("http://localhost:8080/csrf", {
      credentials: "include", // 쿠키 포함
    });
    const data = await response.json();
    setCsrfToken(data.token); // 서버에서 받은 CSRF 토큰 설정
  };

  useEffect(() => {
    fetchCsrfToken(); // 컴포넌트가 마운트될 때 CSRF 토큰을 가져옴
  }, []);

  console.log("csrfToken :::::::", csrfToken);

  const apiResponse = async (url: string) => {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "X-CSRF-TOKEN": csrfToken, // 헤더에 CSRF 토큰 포함
      },
      credentials: "include",
    });
    const data = await response.json();
    if (response.ok) {
      console.log("요청 성공:", data);
    } else {
      console.error("요청 실패");
    }
  };

  const logOut = async () => {
    fetch("http://localhost:8080/logout", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded", // 필요시 다른 헤더도 포함
      },
      credentials: "include", // 쿠키/세션 정보 포함
    }).then((response) => {
      if (response.ok) {
        console.log("Logged out successfully");
      }
    });
  };

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

  return (
    <div>
      <p>
        {csrfToken}
        <button
          onClick={() => {
            fetchCsrfToken();
          }}
        >
          Refresh CSRF Token
        </button>
      </p>
      <p>
        <button onClick={() => apiResponse("http://localhost:8080/all")}>
          All Access
        </button>
      </p>
      <p>
        <button onClick={() => apiResponse("http://localhost:8080/no-login")}>
          No Login
        </button>
      </p>
      <p>
        <button
          onClick={() => apiResponse("http://localhost:8080/has-certified")}
        >
          Login User
        </button>
      </p>
      <p>
        <button onClick={() => apiResponse("http://localhost:8080/admin")}>
          Admin
        </button>
      </p>
      <p>
        <button onClick={() => apiResponse("http://localhost:8080/user")}>
          User
        </button>
      </p>
      <hr />
      <p>
        <button onClick={() => logIn()}>Log-In</button>
      </p>
      <p>
        <button onClick={() => logOut()}>Log-Out</button>
      </p>
    </div>
  );
}
