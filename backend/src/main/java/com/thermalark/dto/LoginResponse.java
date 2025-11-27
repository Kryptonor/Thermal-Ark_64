package com.thermalark.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String message;
    
    public LoginResponse(String token, String username, String role, String message) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.message = message;
    }
    
    public static LoginResponse success(String token, String username, String role) {
        return new LoginResponse(token, username, role, "登录成功");
    }
}