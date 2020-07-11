package com.connect.api.connect4.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MonteCarloResult {

    private List<Integer> moves;

    private List<Integer> values;

    private int bestMove;
}
