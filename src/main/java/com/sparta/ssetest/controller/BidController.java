package com.sparta.ssetest.controller;

import com.sparta.ssetest.dto.BidRequest;
import com.sparta.ssetest.dto.BidResponse;
import com.sparta.ssetest.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BidController {

    private final BidService bidService;

    @GetMapping(value = "/connect/{auctionId}", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SseEmitter> subscribe(
                                                @PathVariable Long auctionId,
                                                // @RequestHeader를 이용하여 header를 받아 데이터를 꺼내서 사용
                                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok(bidService.subscribe(auctionId, lastEventId));
    }

    @PostMapping("/{userId}/auction/{auctionId}")
    public ResponseEntity<BidResponse> createBid (@PathVariable Long userId, @PathVariable Long auctionId,@RequestBody BidRequest request) {
        return ResponseEntity.ok(bidService.createBid(userId, auctionId, request));
    }

}
