package com.connect.api.dto.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "game_history")
public class State implements Serializable {

    @EmbeddedId
    private StateID stateID;

    @Column(name = "whose_turn")
    private Integer whoseTurn;

    private String state;

    @MapsId("gameId")
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game gameData;

}


