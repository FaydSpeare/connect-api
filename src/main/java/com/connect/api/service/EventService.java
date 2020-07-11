package com.connect.api.service;

import com.connect.api.dto.event.InitGameEvent;
import com.connect.api.dto.event.OnlineCountEvent;
import com.connect.api.dto.response.NewSubscriberResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
public class EventService {

    public static final String UPDATE_EVENT = "UPDATE";
    public static final String INIT_EVENT = "INIT";
    public static final String CHAT_EVENT = "CHAT";
    public static final String ONLINE_COUNT_EVENT = "ONLINE";
    public static final String INVITE_EVENT = "INVITE";

    private static AtomicLong idCounter = new AtomicLong();

    public static Long getNextID() {
        return idCounter.getAndIncrement();
    }

    private ConcurrentHashMap<Long, SseEmitter> subscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<Long>> subscribersByGame = new ConcurrentHashMap<>();

    public SseEmitter createSubscriber(Long subscriberId) {
        SseEmitter subscriber = new SseEmitter(-1L);
        subscriber.onCompletion(() -> {
            log.info("COMPLETED subscriber with ID: {}", subscriberId);
            subscribers.remove(subscriberId);
        });
        subscriber.onError((error) -> {
            log.info("ERROR subscriber with ID: {}", subscriberId);
            subscribers.remove(subscriberId);
        });
        subscriber.onTimeout(() -> {
            log.info("TIMEOUT subscriber with ID: {}", subscriberId);
            subscribers.remove(subscriberId);
        });
        this.subscribe(subscriber, subscriberId);
        return subscriber;
    }

    public void notifySubscribers(Long gameId, Object object, String name) {

        if (subscribersByGame.containsKey(gameId)) {
            for (Long subscriberId: subscribersByGame.get(gameId)) {
                SseEmitter subscriber = subscribers.get(subscriberId);
                if (subscriber != null) {
                    try {
                        subscriber.send(SseEmitter.event().name(name).data(object, MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        unsubscribeFromGame(subscriberId, gameId);
                        log.info("Removing subscriber due to IOException");
                    }
                }
            }
        }

    }

    private void subscribe(SseEmitter subscriber, Long subscriberId) {
        if (subscribers.containsKey(subscriberId)) {
            subscribers.replace(subscriberId, subscriber);
        } else {
            subscribers.put(subscriberId, subscriber);
        }
    }

    public void subscribeToGame(Long subscriberId, Long gameId) {
        if (subscribers.containsKey(subscriberId)) {
            if (!subscribersByGame.containsKey(gameId)) {
                subscribersByGame.put(gameId, new CopyOnWriteArrayList<>());
            }
            subscribersByGame.get(gameId).add(subscriberId);
        }
    }

    private void unsubscribeFromGame(Long subscriberId, Long gameId) {
        if (subscribers.containsKey(subscriberId)) {
            if (subscribersByGame.containsKey(gameId)) {
                subscribersByGame.get(gameId).remove(subscriberId);
                if (subscribersByGame.get(gameId).isEmpty()) {
                    subscribersByGame.remove(gameId);
                }
            }
        }
    }

    public NewSubscriberResponse getNewSubscriberId() {
        return new NewSubscriberResponse(getNextID());
    }

    public void notifyAllSubscribers(Object object, String name) {
        for (SseEmitter subscriber: subscribers.values()) {
            try {
                subscriber.send(SseEmitter.event().name(name).data(object, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                log.info("Removing subscriber due to IOException");
            }
        }
    }

    public boolean notifySubscriber(Long subscriberId, Object object, String name) {
        SseEmitter subscriber = subscribers.get(subscriberId);
        if (subscriber != null) {
            try {
                subscriber.send(SseEmitter.event().name(name).data(object, MediaType.APPLICATION_JSON));
                return true;
            } catch (IOException e) {
                log.info("Removing subscriber due to IOException");
                return false;
            }
        }
        return false;
    }

    public void unsubscribe(Long subscriberId) {
        Optional.of(subscriberId).map(x -> subscribers.get(x)).ifPresent(ResponseBodyEmitter::complete);
        subscribers.remove(subscriberId);
    }

    public int getOnlineUsersCount() {
        return subscribers.size();
    }

    public boolean isUserOnline(Long userId) {
        return subscribers.containsKey(userId);
    }

    public void showOnlineUsers() {
        for (Long userId: subscribers.keySet()) {
            log.info("UserId: {} is online", userId);
        }
    }

    public void sendOnlineCountUpdate() {
        OnlineCountEvent event = new OnlineCountEvent();
        event.setCount(this.getOnlineUsersCount());
        this.notifyAllSubscribers(event, ONLINE_COUNT_EVENT);
    }
}
