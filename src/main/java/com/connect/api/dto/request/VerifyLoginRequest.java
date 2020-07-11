package com.connect.api.dto.request;

import lombok.Data;

@Data
public class VerifyLoginRequest {

    private String username;

    private String password;

}
