package com.connect.api.dto.request;

import lombok.Data;

@Data
public class CreateGameRequest {

    private Long userId;

    private Boolean botGame;

}
