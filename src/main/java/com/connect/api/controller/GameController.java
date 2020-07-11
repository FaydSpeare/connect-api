package com.connect.api.controller;

import com.connect.api.dto.event.GameEvent;
import com.connect.api.dto.event.InitGameEvent;
import com.connect.api.dto.request.UpdateBoardRequest;
import com.connect.api.dto.response.InitGameResponse;
import com.connect.api.dto.response.RunningGamesResponse;
import com.connect.api.dto.response.SpectateGameResponse;
import com.connect.api.service.BotService;
import com.connect.api.service.EventService;
import com.connect.api.service.GameService;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.sql.DataSource;

import static com.connect.api.service.EventService.INIT_EVENT;
import static com.connect.api.service.EventService.UPDATE_EVENT;
import static org.springframework.orm.hibernate5.SessionFactoryUtils.getDataSource;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/game")
@Slf4j
public class GameController {

    private final GameService gameService;

    private final EventService eventService;

    private final BotService botService;

    public GameController(GameService gameService, EventService eventService, BotService botService) {
        this.gameService = gameService;
        this.eventService = eventService;
        this.botService = botService;
    }

    @ResponseBody
    @GetMapping(value = "/new-human-game/{userId}")
    public InitGameResponse createHumanGame(@PathVariable Long userId) {
        return gameService.createNewGame(userId, false);
    }

    @ResponseBody
    @GetMapping(value = "/new-ai-game/{userId}")
    public InitGameResponse createAIGame(@PathVariable Long userId) {
        InitGameResponse response = gameService.createNewGame(userId, true);
        gameService.addUserToGame(9999L, response.getGameId());
        return response;
    }

    @GetMapping(value = "/subscribe/{gameId}")
    public SseEmitter subscribe(@PathVariable Long gameId) {
        log.info("Subscribing to gameId: {}", gameId);
        if (gameService.isValidGame(gameId)) {
            return eventService.createSubscriber(gameId);
        }
        log.info("No game with gameId: {}", gameId);
        return null;
    }

    @ResponseBody
    @GetMapping(value = "/{userId}/join/{gameId}")
    public InitGameResponse joinGame(@PathVariable Long userId, @PathVariable Long gameId) {
        return gameService.addUserToGame(userId, gameId);
    }

    @ResponseBody
    @GetMapping(value = "/spectate/{gameId}")
    public SpectateGameResponse spectateGame(@PathVariable Long gameId) {
        return gameService.spectateGame(gameId);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/update")
    public void updateGame(@RequestBody UpdateBoardRequest request) {
        gameService.updateGame(request);
        if (gameService.isAiGame(request.getGameId())) {
            botService.ai(request.getGameId());
        }
    }


}
