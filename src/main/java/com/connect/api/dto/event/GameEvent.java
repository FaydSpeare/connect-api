package com.connect.api.dto.event;

import lombok.Data;

import java.util.List;

@Data
public class GameEvent {

    public List<List<Integer>> board;

    public Long outcome;

    public Integer turn;

    public List<Integer> winCombination;

}
