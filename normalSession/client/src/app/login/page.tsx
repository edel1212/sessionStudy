"use client";
import React, { useState, useEffect } from "react";

export default function Login() {
  const [id, setId] = useState("");
  const [pw, setPw] = useState("");
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

  const loginFrom = async () => {
    const formData = new URLSearchParams();
    formData.append("username", id);
    formData.append("password", pw);
    formData.append("_csrf", csrfToken); // CSRF 토큰을 본문에 추가

    const response = await fetch("http://localhost:8080/login", {
      method: "POST",
      headers: {
        //'X-XSRF-TOKEN': csrfToken,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString(),
      credentials: "include", // 쿠키 포함
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
      <label>
        id input:{" "}
        <input name="id" value={id} onChange={(e) => setId(e.target.value)} />
      </label>
      <hr />
      <label>
        pw input:{" "}
        <input name="pw" value={pw} onChange={(e) => setPw(e.target.value)} />
      </label>
      <hr />
      <button onClick={loginFrom}>로그인</button>
    </div>
  );
}
