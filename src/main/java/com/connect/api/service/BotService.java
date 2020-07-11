package com.connect.api.service;

import com.connect.api.connect4.BoardUtils;
import com.connect.api.connect4.ai.MonteCarlo;
import com.connect.api.connect4.ai.MonteCarloResult;
import com.connect.api.dto.entity.Game;
import com.connect.api.dto.event.ChatEvent;
import com.connect.api.dto.event.InviteEvent;
import com.connect.api.dto.request.UpdateBoardRequest;
import com.connect.api.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.connect.api.service.EventService.CHAT_EVENT;
import static com.connect.api.service.EventService.INVITE_EVENT;

@Slf4j
@Service
@Transactional
public class BotService {

    private final GameRepository gameRepository;

    private final GameService gameService;

    private final EventService eventService;

    private final UserService userService;

    public BotService(GameRepository gameRepository, GameService gameService, EventService eventService, UserService userService) {
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.eventService = eventService;
        this.userService = userService;
    }


    @Async
    public void ai(Long gameId) {
        Optional<Game> result = gameRepository.findById(gameId);

        if (result.isPresent()) {
            Game game = result.get();

            if (game.getOutcome() == null && (game.getWhoseTurn() == 2 && game.getPlayerTwo().isAI()) ||
                    (game.getWhoseTurn() == 1 && game.getPlayerOne().isAI())) {

                log.info("Calculating AI Move");

                int n = 10000;
                List<List<Integer>> board = BoardUtils.fromBoardString(game.getCurrentState());
                MonteCarloResult monteCarloResult = MonteCarlo.monteCarlo(board, game.getWhoseTurn(), n);

                int col = monteCarloResult.getBestMove() % 7;
                int row = (monteCarloResult.getBestMove() - col) / 7;

                UpdateBoardRequest request = new UpdateBoardRequest();

                List<Integer> rowList = board.get(row);
                rowList.set(col, game.getWhoseTurn());
                board.set(row, rowList);

                request.setBoard(board);
                request.setGameId(gameId);

                ChatEvent chatEvent = new ChatEvent();
                chatEvent.setUsername("Celestial");

                StringBuilder chatLine = new StringBuilder(String.format("Results: (BestMove: %s)", monteCarloResult.getBestMove()));
                for (int i = 0; i < monteCarloResult.getMoves().size(); i++) {
                    double eval = (double) monteCarloResult.getValues().get(i) / (double) n;
                    chatLine.append(String.format("\n(Move: %s -> %s)", monteCarloResult.getMoves().get(i), eval));
                }

                chatEvent.setChatLine(chatLine.toString());

                eventService.notifySubscribers(gameId, chatEvent, CHAT_EVENT);

                gameService.updateGame(request);
            }

        }
    }

    public boolean isChatCommand(String chatLine) {
        return chatLine.startsWith(":user") || chatLine.startsWith(":invite-join") || chatLine.startsWith(":invite-spectate");
    }

    public void sendUserCommandEvent(String chatLine, Long userId) {
        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setUsername("SYSTEM");

        String username = chatLine.substring(6);
        Long searchedUserId = userService.getUserIdFromUsername(username);
        if (searchedUserId == null) {
            chatEvent.setChatLine(String.format("Couldn't find user [%s]", username));
        } else {
            boolean isUserOnline = eventService.isUserOnline(searchedUserId);
            chatEvent.setChatLine(String.format("%s %s", username, isUserOnline ? "is online" : "isn't online"));
        }
        eventService.notifySubscriber(userId, chatEvent, CHAT_EVENT);
    }

    public void sendInviteCommandEvent(String chatLine, Long userId, Long gameId, int index, boolean join) {
        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setUsername("SYSTEM");
        String username = chatLine.substring(index);
        Long searchedUserId = userService.getUserIdFromUsername(username);
        if (searchedUserId == null) {
            chatEvent.setChatLine(String.format("Couldn't find user [%s]", username));
        } else {
            boolean isUserOnline = eventService.isUserOnline(searchedUserId);
            if (isUserOnline) {
                InviteEvent inviteEvent = new InviteEvent();
                inviteEvent.setJoin(join);
                inviteEvent.setSpectate(!join);
                inviteEvent.setGameId(gameId);
                inviteEvent.setUsername(userService.getUsername(userId));
                chatEvent.setChatLine("Sent invite to " + username);
                eventService.notifySubscriber(searchedUserId, inviteEvent, INVITE_EVENT);
            } else {
                chatEvent.setChatLine(username + " is not online. Invite not sent.");
            }
        }
        eventService.notifySubscriber(userId, chatEvent, CHAT_EVENT);
    }

    public void sendChatCommandEvent(String chatLine, Long userId, Long gameId) {
        if (chatLine.startsWith(":user ")) {
            this.sendUserCommandEvent(chatLine, userId);
        } else if (chatLine.startsWith(":invite-join")) {
            this.sendInviteCommandEvent(chatLine, userId, gameId, 13, true);
        } else if (chatLine.startsWith(":invite-spectate")) {
            this.sendInviteCommandEvent(chatLine, userId, gameId, 17, false);
        }
    }
}
