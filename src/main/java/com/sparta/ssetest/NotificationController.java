package com.sparta.ssetest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/connect", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SseEmitter> subscribe(@PathVariable Long userId,
                                                // @RequestHeader를 이용하여 header를 받아 데이터를 꺼내서 사용
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok(notificationService.subscribe(userId, lastEventId));
    }
}
