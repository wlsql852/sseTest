package com.sparta.ssetest.controller;

import com.sparta.ssetest.entity.User;
import com.sparta.ssetest.repository.UserRepository;
import com.sparta.ssetest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping(value = "/connect", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SseEmitter> subscribe(@PathVariable Long userId,
                                                // @RequestHeader를 이용하여 header를 받아 데이터를 꺼내서 사용
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new NullPointerException("User not found"));
        return ResponseEntity.ok(notificationService.subscribe(user, lastEventId));
    }
}
