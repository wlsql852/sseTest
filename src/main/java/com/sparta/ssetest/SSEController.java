package com.sparta.ssetest;

import com.sparta.ssetest.dto.BidRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SSEController {
    private final SseEmitterService sseEmitterService;

    //응답 mime type 은 반드시 text/event-stream 이여야 한다.
    //클라이언트로 부터 SSE subscription 을 수락한다.
    @GetMapping(path = "/v1/sse/subscribe/{auctionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @PathVariable Long auctionId,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        String sseId = UUID.randomUUID().toString();
        SseEmitter emitter = sseEmitterService.subscribe(auctionId, sseId, lastEventId);
        return ResponseEntity.ok(emitter);
    }

    //eventPayload 를 SSE 로 연결된 모든 클라이언트에게 broadcasting 한다.
    @PostMapping(path = "/v1/sse/broadcast/{auctionId}")
    public ResponseEntity<Void> broadcast(@PathVariable Long auctionId, @RequestBody BidRequest request) {
        sseEmitterService.broadcast(auctionId, request);
        return ResponseEntity.ok().build();
    }
}
