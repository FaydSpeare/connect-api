package com.connect.api.service;

import com.connect.api.connect4.BoardUtils;
import com.connect.api.dto.entity.Game;
import com.connect.api.dto.entity.User;
import com.connect.api.dto.event.ChatEvent;
import com.connect.api.dto.event.GameEvent;
import com.connect.api.dto.event.InitGameEvent;
import com.connect.api.dto.event.PlayerInfo;
import com.connect.api.dto.request.UpdateBoardRequest;
import com.connect.api.dto.response.InitGameResponse;
import com.connect.api.dto.response.SpectateGameResponse;
import com.connect.api.repository.GameRepository;
import com.connect.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.connect.api.connect4.BoardUtils.START_POS;
import static com.connect.api.service.EventService.*;

@Slf4j
@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;

    private final UserRepository userRepository;

    private final EventService eventService;

    public GameService(GameRepository gameRepository, UserRepository userRepository, EventService eventService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public InitGameResponse createNewGame(Long userId, boolean aiGame) {

        log.info("Creating game");

        Optional<User> result = userRepository.findById(userId);

        if (!result.isPresent()) {
            return new InitGameResponse(null, null, null, false);
        }

        Game game = new Game();
        User user = result.get();
        game.setPlayerOne(user);
        game.setWhoseTurn(1);
        game.setCurrentState(START_POS);

        gameRepository.save(game);

        return new InitGameResponse(game.getGameId(), 1L, user.getElo(), true);
    }

    public boolean isValidGame(Long gameId) {
        return gameRepository.existsById(gameId);
    }

    public InitGameResponse addUserToGame(Long userId, Long gameId) {
        Optional<Game> gameResult = gameRepository.findById(gameId);
        Optional<User> userResult = userRepository.findById(userId);

        if (gameResult.isPresent() && userResult.isPresent()) {
            Game game = gameResult.get();
            User user = userResult.get();

            if (game.getPlayerTwo() == null) {
                game.setPlayerTwo(user);
                gameRepository.save(game);
                InitGameEvent initGameEvent = this.getInitGameEvent(gameId);
                eventService.notifySubscribers(gameId, initGameEvent, INIT_EVENT);
                return new InitGameResponse(game.getGameId(), 2L, user.getElo(), true);
            }
        }

        return new InitGameResponse(null, null, null, false);
    }

    public GameEvent updateGame(UpdateBoardRequest request) {

        if (request.getGameId() == null) {
            return null;
        }

        Optional<Game> gameResult = gameRepository.findById(request.getGameId());

        if (gameResult.isPresent()) {
            log.info("Found Game");
            Game game = gameResult.get();

            if (!isGameReadyToStart(game) || isGameOver(game)) {
                return null;
            }

            List<List<Integer>> currentBoard = BoardUtils.fromBoardString(game.getCurrentState());
            List<List<Integer>> updatedBoard = request.getBoard();
            int whoseTurn = game.getWhoseTurn();

            log.info(currentBoard.toString());
            log.info(updatedBoard.toString());

            if (BoardUtils.wasLegalUpdate(currentBoard, updatedBoard, whoseTurn)) {
                log.info("Board Legal");
                log.info("Adding state to history");
                game.addToHistory();
                game.setCurrentState(BoardUtils.toBoardString(updatedBoard));
                game.setWhoseTurn(3 - whoseTurn);

                GameEvent gameEvent = new GameEvent();
                gameEvent.setBoard(updatedBoard);
                gameEvent.setTurn(game.getWhoseTurn());

                Pair<Long, List<Integer>> result = BoardUtils.checkGameOver(updatedBoard);
                if (result != null) {
                    game.setOutcome(result.getFirst());

                    gameEvent.setOutcome(result.getFirst());
                    gameEvent.setWinCombination(result.getSecond());

                    ChatEvent chatEvent = new ChatEvent();
                    if (result.getFirst() == 1) {
                        chatEvent.setChatLine(String.format("Game Over! %s Wins!", game.getPlayerOne().getUsername()));
                    } else if (result.getFirst() == 2) {
                        chatEvent.setChatLine(String.format("Game Over! %s Wins!", game.getPlayerTwo().getUsername()));
                    } else {
                        chatEvent.setChatLine("Game Over! Draw!");
                    }
                    chatEvent.setUsername("SYSTEM");


                    eventService.notifySubscribers(request.getGameId(), chatEvent, CHAT_EVENT);

                    this.adjustElos(game);
                }

                gameRepository.save(game);

                log.info("Board update: {}", gameEvent.getBoard());
                eventService.notifySubscribers(request.getGameId(), gameEvent, UPDATE_EVENT);
                return null;

            }
        }

        // Return null to signify no updates
        return null;
    }

    private void adjustElos(Game game) {
        User playerOne = game.getPlayerOne();
        User playerTwo = game.getPlayerTwo();

        int p1EloDiff = playerTwo.getElo() - playerOne.getElo();
        double p1Score = (((3 - game.getOutcome()) * 2) - 3) * Math.min(game.getOutcome(), 1);

        int p1Elo = playerOne.getElo() + (int) (15 * (p1Score + (p1EloDiff / 400.0)));
        int p2Elo = playerTwo.getElo() + (int) (15 * (-p1Score + (-p1EloDiff / 400.0)));

        // Don't win points for losing
        if (game.getOutcome() == 1 && p2Elo > playerTwo.getElo()) {
            p2Elo = playerTwo.getElo();
        }

        // Don't win points for losing
        if (game.getOutcome() == 2 && p1Elo > playerOne.getElo()) {
            p1Elo = playerOne.getElo();
        }

        log.info("p1Elo: {}, p2Elo: {}", p1Elo, p2Elo);

        playerOne.setElo(p1Elo);
        playerTwo.setElo(p2Elo);
    }


    public InitGameEvent getInitGameEvent(Long gameId) {

        if (gameId == null) {
            return null;
        }

        Optional<Game> gameResult = gameRepository.findById(gameId);

        if (gameResult.isPresent()) {

            Game game = gameResult.get();
            InitGameEvent initGameEvent = new InitGameEvent();
            List<PlayerInfo> playerList = getPlayerInfos(game);
            initGameEvent.setPlayers(playerList);
            return initGameEvent;

        }

        return null;
    }

    private boolean isGameReadyToStart(Game game) {
        return game.getPlayerOne() != null && game.getPlayerTwo() != null;
    }

    private boolean isGameOver(Game game) {
        return game.getOutcome() != null;
    }

    public boolean isAiGame(Long gameId) {

        Optional<Game> result = gameRepository.findById(gameId);

        if (result.isPresent()) {
            Game game = result.get();
            if (isGameReadyToStart(game)) {
                return game.getPlayerOne().getUserId() == 9999L || game.getPlayerTwo().getUserId() == 9999L;
            }
        }

        return false;
    }

    public SpectateGameResponse spectateGame(Long gameId) {
        Optional<Game> result = gameRepository.findById(gameId);

        if (result.isPresent()) {
            Game game = result.get();

            SpectateGameResponse response = new SpectateGameResponse();

            List<PlayerInfo> playerList = getPlayerInfos(game);
            response.setPlayers(playerList);

            response.setBoard(BoardUtils.fromBoardString(game.getCurrentState()));
            return response;

        }
        return null;
    }

    private List<PlayerInfo> getPlayerInfos(Game game) {

        if (game.getPlayerOne() == null || game.getPlayerTwo() == null) {
            return null;
        }

        List<PlayerInfo> playerList = new ArrayList<>();

        PlayerInfo p1 = new PlayerInfo();
        p1.setElo(game.getPlayerOne().getElo());
        p1.setUsername(game.getPlayerOne().getUsername());

        PlayerInfo p2 = new PlayerInfo();
        p2.setElo(game.getPlayerTwo().getElo());
        p2.setUsername(game.getPlayerTwo().getUsername());

        playerList.add(p1);
        playerList.add(p2);
        return playerList;
    }

    public Integer getPastGameCount(User user) {
        return gameRepository.getPastGameCount(user);
    }
}
