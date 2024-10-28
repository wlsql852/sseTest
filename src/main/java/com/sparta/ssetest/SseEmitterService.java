package com.sparta.ssetest;

import com.sparta.ssetest.dto.BidRequest;
import com.sparta.ssetest.entity.Auction;
import com.sparta.ssetest.entity.Bid;
import com.sparta.ssetest.repository.AuctionRepository;
import com.sparta.ssetest.repository.BidRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {
    // thread-safe 한 컬렉션 객체로 sse emitter 객체를 관리해야 한다.
//    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, SseEmitter>> listMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000 * 5;   //5분
    private static final long RECONNECTION_TIMEOUT = 1000L;

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;

    public SseEmitterService(BidRepository bidRepository, AuctionRepository auctionRepository) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
    }

    public SseEmitter subscribe(Long auctionId, String id, String lastEventId) {
        //해당 경매장의 emitter 관리 컬렉션 객체 가져오기(없으면 생성)
        if (!listMap.containsKey(auctionId)) {
            listMap.put(auctionId, new ConcurrentHashMap<>());
        }
        Map<String, SseEmitter> emitterMap = listMap.get(auctionId);

        SseEmitter emitter = createEmitter();
        //연결 세션 timeout 이벤트 핸들러 등록
        emitter.onTimeout(() -> {
            log.info("server sent event timed out : id={}", id);
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //에러 핸들러 등록
        emitter.onError(e -> {
            log.info("server sent event error occurred : id={}, message={}", id, e.getMessage());
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            if (emitterMap.remove(id) != null) {
                log.info("server sent event removed in emitter cache: id={}", id);
            }

            log.info("disconnected by completed server sent event: id={}", id);
        });

        emitterMap.put(id, emitter);

        //초기 연결시에 응답 데이터를 전송할 수도 있다.
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    //event 명 (event: event example)
                    .name("connection alert")
                    //event id (id: id-1) - 재연결시 클라이언트에서 `Last-Event-ID` 헤더에 마지막 event id 를 설정
                    .id(String.valueOf("id-1"))
                    //event data payload (data: SSE connected)
                    .data("SSE connected")
                    //SSE 연결이 끊어진 경우 재접속 하기까지 대기 시간 (retry: <RECONNECTION_TIMEOUT>)
                    .reconnectTime(RECONNECTION_TIMEOUT);
            emitter.send(event);
        } catch (IOException e) {
            log.error("failure send media position data, id={}, {}", id, e.getMessage());
        }
        return emitter;
    }

    public void broadcast(Long auctionId, BidRequest request) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(()-> new NullPointerException("Auction not found"));
        Map<String, SseEmitter> emitterMap = listMap.get(auctionId);
        Bid newBid = new Bid(auction, request.getPrice());
        bidRepository.save(newBid);
        emitterMap.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("new price")
                        .id("broadcast event 1")
                        .reconnectTime(RECONNECTION_TIMEOUT)
                        .data(request.getPrice()));
                log.info("sended notification, id={}, new price={}", id, request.getPrice());
            } catch (IOException e) {
                //SSE 세션이 이미 해제된 경우
                log.error("fail to send emitter id={}, {}", id, e.getMessage());
            }
        });
    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);
    }
}
