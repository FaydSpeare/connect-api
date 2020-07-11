package com.connect.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    public boolean valid;

    public Long user;

    public String errorMessage;

}
