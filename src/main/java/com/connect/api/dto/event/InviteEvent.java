package com.connect.api.dto.event;

import lombok.Data;

@Data
public class InviteEvent {

    private Long gameId;

    private boolean join;

    private boolean spectate;

    private String username;

}
