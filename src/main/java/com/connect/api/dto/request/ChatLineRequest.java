package com.connect.api.dto.request;

import lombok.Data;

@Data
public class ChatLineRequest {

    private Long gameId;

    private Long userId;

    private String chatLine;

}
