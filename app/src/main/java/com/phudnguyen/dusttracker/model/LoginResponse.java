package com.phudnguyen.dusttracker.model;

public class LoginResponse extends BaseResponse {
    private String token;
    private UserInfo user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}
