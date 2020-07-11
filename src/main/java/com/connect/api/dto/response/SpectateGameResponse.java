package com.connect.api.dto.response;

import com.connect.api.dto.event.PlayerInfo;
import lombok.Data;

import java.util.List;

@Data
public class SpectateGameResponse {

    public List<PlayerInfo> players;

    public List<List<Integer>> board;

}
