package com.appmsg.front.appmensajeriafront.model.auth;

import com.appmsg.front.appmensajeriafront.model.ResponseBase;

public class LoginRS extends ResponseBase {

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
