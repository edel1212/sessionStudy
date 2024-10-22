"use client";

import { useState } from "react";

export default function RedisHeaderSession() {
  const [responseData, setResponseData] = useState(""); // 응답 데이터를 저장할 상태 추가
  const [xAtuhToken, setXAuthToken] = useState("");

  const apiResponse = async (url: string) => {
    debugger;
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "x-auth-token": xAtuhToken,
      },
      credentials: "include",
    });
    const data = await response.json();
    if (response.ok) {
      console.log("요청 성공:", data);
      setResponseData(JSON.stringify(data, null, 2)); // JSON 데이터를 문자열로 변환하여 상태에 저장
    } else {
      console.error("요청 실패");
      setResponseData("Error: 요청에 실패했습니다."); // 오류 메시지 출력
    }
  };

  const logOut = async () => {
    fetch("http://localhost:8080/member/logout", {
      method: "POST",
      headers: {
        "x-auth-token": xAtuhToken,
      },
      credentials: "include", // 쿠키/세션 정보 포함
    }).then((response) => {
      if (response.ok) {
        alert("로그아웃 성공!");
        console.log("Logged out successfully");
        setResponseData("Logged out successfully"); // 로그아웃 결과 출력
      }
    });
  };

  const logIn = async () => {
    const formData = new URLSearchParams();
    formData.append("username", "yoo");
    formData.append("password", "123");

    const response = await fetch("http://localhost:8080/member/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData.toString(),
    });
    const data = await response.json();
    console.log(data);
    if (response.ok) {
      console.log("로그인 성공:", data);
      setResponseData(JSON.stringify(data, null, 2));
      debugger;
      // Session Token 값 반환
      setXAuthToken(data.xAuthToken);
    } else {
      console.error("로그인 실패");
      setResponseData("Error: 로그인에 실패했습니다."); // 오류 메시지 출력
    }
  };

  return (
    <main style={{ display: "flex", justifyContent: "space-between" }}>
      <section style={{ width: "100vw", border: "1px solid" }}>
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
      </section>
      <section style={{ width: "100vw", border: "1px solid" }}>
        <textarea
          style={{ width: "50vw", height: "50vh" }}
          value={responseData} // 상태값을 textarea에 표시
          readOnly
        ></textarea>
      </section>
    </main>
  );
}
