package com.phudnguyen.dusttracker.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse extends BaseResponse {
    @SerializedName("user")
    private UserInfo userInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
