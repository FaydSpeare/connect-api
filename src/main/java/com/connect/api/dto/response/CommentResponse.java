package com.connect.api.dto.response;

import lombok.Data;

@Data
public class CommentResponse {

    private String commentor;

    private String comment;

    private String date;

}
