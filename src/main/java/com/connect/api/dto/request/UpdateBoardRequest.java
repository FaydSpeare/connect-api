package com.connect.api.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateBoardRequest {

    public Long gameId;

    public List<List<Integer>> board;

}
