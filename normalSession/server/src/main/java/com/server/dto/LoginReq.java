package com.server.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LoginReq {
    private String id;
    private String pw;
}
