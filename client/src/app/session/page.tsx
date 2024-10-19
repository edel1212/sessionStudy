"use client";
import React, { useState, useEffect } from "react";

export default function Session() {

  const sendReqeust = async () => {
    const response = await fetch("http://localhost:8080/session", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      credentials: "include", // 쿠키 포함
    });

    const data = await response.text();
    console.log(data);
    if (response.ok) {
      console.log( data);
    } 
  };

  return (
    <div>
      <button onClick={()=> sendReqeust()}>send</button>
    </div>
  );
}
