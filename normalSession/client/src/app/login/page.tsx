"use client";
import React, { useState } from "react";

export default function Login() {
  const [id, setId] = useState("");
  const [pw, setPw] = useState("");

  const login = async () => {
    const response = await fetch("http://localhost:8080/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        id: id,
        password: pw,
      }),
    });

    if (response.ok) {
      const data = await response.json();
      console.log("로그인 성공:", data);
      // 로그인 성공 후 처리 (예: 페이지 이동)
    } else {
      console.error("로그인 실패");
      // 로그인 실패 처리
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
      <button>로그인</button>
    </div>
  );
}
