package com.sparta.ssetest;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    // SseEmitter 저장소
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 이벤트 캐시 저장소
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        return emitter;
    }

    @Override
    public void deleteById(String emitterId) {
        emitters.remove(emitterId);
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithUserId(String userId) {
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void saveEvent(String eventId, Object event) {
        eventCache.put(eventId, event);
    }

    @Override
    public void deleteAllEventCacheByUserId(String userId) {
        eventCache.keySet().removeIf(key -> key.startsWith(userId));
    }
}
