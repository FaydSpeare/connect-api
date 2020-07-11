package com.connect.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitGameResponse {

    public Long gameId;

    public Long playerNumber;

    public Integer elo;

    public boolean success;

}
