package com.sparta.ssetest.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(String id, SseEmitter sseEmitter);

    void saveEventCache(String eventCacheId, Object event);

    Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);

    Map<String, Object> findAllEventCacheStartWithUserId(String userId);

    void deleteById(String id);

    void deleteAllEmitterStartWithUserId(String userId);

    void deleteAllEventCacheStartWithId(String userId);

}
