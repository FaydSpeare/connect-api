package com.connect.api.dto.request;

import lombok.Data;

@Data
public class CommentRequest {

    private String comment;

    private Long userId;

    private Long commentorId;

}
