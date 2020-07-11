package com.connect.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateDetailsResponse {

    private boolean valid;

    private String errorMessage;

}
