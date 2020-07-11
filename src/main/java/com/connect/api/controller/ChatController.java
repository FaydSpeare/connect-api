package com.connect.api.controller;

import com.connect.api.dto.event.ChatEvent;
import com.connect.api.dto.request.ChatLineRequest;
import com.connect.api.service.BotService;
import com.connect.api.service.EventService;
import com.connect.api.service.GameService;
import com.connect.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.connect.api.service.EventService.CHAT_EVENT;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/chat")
@Slf4j
public class ChatController {

    private final EventService eventService;

    private final UserService userService;

    private final GameService gameService;

    private final BotService botService;

    public ChatController(EventService eventService, UserService userService, GameService gameService, BotService botService) {
        this.eventService = eventService;
        this.userService = userService;
        this.gameService = gameService;
        this.botService = botService;
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/chatLine")
    public void addChatLine(@RequestBody ChatLineRequest request) {
        log.info("ChatLine received for game: {}", request.getGameId());
        if (gameService.isValidGame(request.getGameId())) {
            if (botService.isChatCommand(request.getChatLine())) {
                botService.sendChatCommandEvent(request.getChatLine(), request.getUserId(), request.getGameId());
            } else {
                ChatEvent chatEvent = new ChatEvent();
                chatEvent.setChatLine(request.getChatLine());
                chatEvent.setUsername(userService.getUsername(request.getUserId()));
                log.info("{} {}", chatEvent.getUsername(),chatEvent.getChatLine());
                eventService.notifySubscribers(request.getGameId(), chatEvent, CHAT_EVENT);
            }
        }

    }
}
