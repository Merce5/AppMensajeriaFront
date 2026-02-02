package com.appmsg.front.appmensajeriafront.model.auth;

import com.appmsg.front.appmensajeriafront.model.ResponseBase;

public class RegisterRS extends ResponseBase {

    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
