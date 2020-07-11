package com.connect.api.dto.event;

import lombok.Data;

import java.util.List;

@Data
public class InitGameEvent {

    public List<PlayerInfo> players;

}
