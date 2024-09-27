"use client";
import React, { useState } from "react";

export default function Login() {
  const [id, setId] = useState("");
  const [pw, setPw] = useState("");

  const loginFrom = async () => {
    /**
     * ℹ️ Form 방식으로 전달 해야 로그인이 성공함
     */
    // URLSearchParams 객체 생성
    const formData = new URLSearchParams();
    formData.append("username", id);
    formData.append("password", pw);

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
      <button
        onClick={() => {
          loginFrom();
        }}
      >
        로그인
      </button>
    </div>
  );
}
