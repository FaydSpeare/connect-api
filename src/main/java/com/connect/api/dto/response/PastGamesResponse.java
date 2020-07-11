package com.connect.api.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PastGamesResponse {

    public List<List<List<Integer>>> gameHistory;

    public Boolean outcome;

    public String opponent;

    public Integer opponentElo;

    public Long gameId;

}
