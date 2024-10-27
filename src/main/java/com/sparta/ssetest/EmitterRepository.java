package com.sparta.ssetest;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {

    // SseEmitter 인스턴스를 저장
    SseEmitter save(String emitterId, SseEmitter emitter);

    // Emitter ID를 통해 SseEmitter 삭제
    void deleteById(String emitterId);

    // 특정 사용자와 연관된 모든 이벤트 캐시 조회
    Map<String, Object> findAllEventCacheStartWithUserId(String userId);

    // 특정 이벤트를 저장
    void saveEvent(String eventId, Object event);

    // 특정 사용자의 모든 이벤트 캐시 삭제
    void deleteAllEventCacheByUserId(String userId);
}
