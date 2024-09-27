"use client";
export default function Home() {
  const textResponse = async (url: string) => {
    const response = await fetch(url);
    const data = await response.text();

    if (response.ok) {
      console.log("요청 성공:", data);
    } else {
      console.error("요청 실패");
    }
  };

  const jsonResponse = async (url: string) => {
    const response = await fetch(url, {
      method: "GET",
      credentials: "include", // 세션 쿠키 포함
    });
    const data = await response.json();
    if (response.ok) {
      console.log("요청 성공:", data);
    } else {
      console.error("요청 실패");
    }
  };

  return (
    <div>
      <p>
        <button onClick={() => textResponse("http://localhost:8080/all")}>
          All Access
        </button>
      </p>
      <p>
        <button onClick={() => textResponse("http://localhost:8080/no-login")}>
          No Login
        </button>
      </p>
      <p>
        <button
          onClick={() => jsonResponse("http://localhost:8080/has-certified")}
        >
          Login User
        </button>
      </p>
      <p>
        <button onClick={() => jsonResponse("http://localhost:8080/admin")}>
          Admin
        </button>
      </p>
      <p>
        <button onClick={() => jsonResponse("http://localhost:8080/user")}>
          User
        </button>
      </p>
    </div>
  );
}
