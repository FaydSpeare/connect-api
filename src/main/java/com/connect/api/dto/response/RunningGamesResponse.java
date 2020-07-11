package com.connect.api.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RunningGamesResponse {

    List<Long> gameIds;

}
