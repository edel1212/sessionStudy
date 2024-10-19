"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

export default function Home() {
  return (
    <main>
      <h1>Sessio - Study</h1>
      <Link href={"/normal-session"}>일반 Session 방식 - Cookie 사용</Link>
      <hr />
      <Link href={"/redis-cookie-session"}>
        Redis Session 방식 - Cookie 사용
      </Link>
      <hr />
    </main>
  );
}
