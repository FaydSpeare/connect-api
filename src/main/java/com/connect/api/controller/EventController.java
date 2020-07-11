package com.connect.api.controller;

import com.connect.api.dto.event.OnlineCountEvent;
import com.connect.api.dto.response.NewSubscriberResponse;
import com.connect.api.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.connect.api.service.EventService.ONLINE_COUNT_EVENT;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/event")
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(value = "/new-subscriber-id")
    public NewSubscriberResponse getNewSubscriberId() {
        return eventService.getNewSubscriberId();
    }

    @GetMapping(value = "/subscribe/{subscriberId}")
    public SseEmitter subscribe(@PathVariable Long subscriberId) {
        log.info("Adding event subscriber with id: {}", subscriberId);
        SseEmitter subscriber = eventService.createSubscriber(subscriberId);
        eventService.sendOnlineCountUpdate();
        return subscriber;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/subscribe/{subscriberId}/to/{gameId}")
    public void subscribeToGame(@PathVariable Long subscriberId, @PathVariable Long gameId) {
        log.info("Adding event subscriber with id: {} to gameId: {}", subscriberId, gameId);
        eventService.subscribeToGame(subscriberId, gameId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/dummy-event")
    public void sendDummyEvent() {
        //eventService.notifyAllSubscribers("Hello");
        eventService.showOnlineUsers();
    }

    @ResponseBody
    @GetMapping(value = "/open/{subscriberId}")
    public Boolean sendOpenEvent(@PathVariable Long subscriberId) {
        log.info("Sending Open Event to subscriber: {}", subscriberId);

        OnlineCountEvent event = new OnlineCountEvent();
        event.setCount(eventService.getOnlineUsersCount());
        eventService.notifySubscriber(subscriberId, event, ONLINE_COUNT_EVENT);

        return eventService.notifySubscriber(subscriberId, "open", "OPEN");
    }

    @ResponseBody
    @GetMapping(value = "/close/{subscriberId}")
    public void closeEventSource(@PathVariable Long subscriberId) {
        eventService.unsubscribe(subscriberId);
        eventService.sendOnlineCountUpdate();
    }

}
