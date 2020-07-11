package com.connect.api.dto.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;

    @ManyToOne
    @JoinColumn(name = "player_one")
    private User playerOne;

    @ManyToOne
    @JoinColumn(name = "player_two")
    private User playerTwo;

    private Long outcome;

    @Column(name = "current_state")
    private String currentState;

    @Column(name = "whose_turn")
    private Integer whoseTurn;

    @OneToMany(mappedBy = "gameData", cascade = {CascadeType.ALL})
    private List<State> stateHistory;

    @Override
    public String toString() {
        return "GameData{" +
                "gameId=" + gameId +
                ", outcome=" + outcome +
                ", currentState='" + currentState + '\'' +
                ", stateHistory=" + stateHistory +
                '}';
    }

    public void addToHistory() {
        long moveNumber = stateHistory.size() + 1;
        State nextState = new State();
        StateID stateId = new StateID();
        stateId.setMoveNumber(moveNumber);
        nextState.setStateID(stateId);
        nextState.setWhoseTurn(this.whoseTurn);
        nextState.setState(this.currentState);
        nextState.setGameData(this);
        this.stateHistory.add(nextState);
    }
}
