package com.sparta.ssetest.service;

import com.sparta.ssetest.dto.BidRequest;
import com.sparta.ssetest.dto.BidResponse;
import com.sparta.ssetest.entity.Auction;
import com.sparta.ssetest.entity.Bid;
import com.sparta.ssetest.entity.User;
import com.sparta.ssetest.repository.AuctionRepository;
import com.sparta.ssetest.repository.BidRepository;
import com.sparta.ssetest.repository.EmitterRepository;
import com.sparta.ssetest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 1;
//    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final long RECONNECTION_TIMEOUT = 1000L;

    //처음 경매장과 연결
    @Transactional
    public SseEmitter subscribe(Long auctionId, String lastEventId) {
        auctionRepository.findById(auctionId).orElseThrow(()-> new NullPointerException("Auction not found"));
        String emitterId = makeTimeIncludeId(auctionId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        String eventId = makeTimeIncludeId(auctionId);
        sendToClient(emitter, "sse", emitterId, eventId,
                "연결되었습니다. EventStream Created. [auctionId=" + auctionId + "]");

        Map<String, Object> events = emitterRepository.findAllEventCacheStartWithAuctionId(
                String.valueOf(auctionId));
        if (!lastEventId.isEmpty()) {
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> sendToClient(emitter,"new price", emitterId, entry.getKey(),
                            entry.getValue()));
        } else {
            events.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry ->
                            sendToClient(emitter,"new price", emitterId, entry.getKey(),
                            entry.getValue()));
        }
        return emitter;
    }

    private String makeTimeIncludeId(Long auctionId) {  // 데이터 유실 시점 파악 위함
        return auctionId + "_"+ System.currentTimeMillis();
    }

    // 특정 SseEmitter 를 이용해 알림을 보냅니다. SseEmitter 는 최초 연결 시 생성되며,
    // 해당 SseEmitter 를 생성한 클라이언트로 알림을 발송하게 됩니다.
    public void sendToClient(SseEmitter emitter,String name, String emitterId, String eventId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(name)
                    .id(eventId)
                    .data(data)
                    .reconnectTime(RECONNECTION_TIMEOUT));
            log.info(eventId+"_"+data);
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            throw new IllegalArgumentException("SSE가 연결되지 않습니다");
        }
    }

    @Transactional
    public BidResponse createBid(Long userId, Long auctionId, BidRequest request) {
        User user = userRepository.findById(userId).orElseThrow(()-> new NullPointerException("User not found"));
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new NullPointerException("Auction not found"));
        List<Bid> bidList = bidRepository.findAllByAuctionOrderByCreatedAtDesc(auction);
        int maxBid = bidList.isEmpty() ? auction.getStartPrice()-1 : bidList.get(0).getPrice();
        if(request.getPrice()<=maxBid) throw new IllegalArgumentException("입찰 금액보다 커야합니다");
        Bid bid = new Bid(request, user, auction);
        Bid newBid = bidRepository.save(bid);
        sseSend(newBid);
        return new BidResponse(newBid);
    }

    @Async
    public void sseSend(Bid bid) {
        String eventId = makeTimeIncludeId(bid.getAuction().getId());
        // 유저의 모든 SseEmitter 가져옴
        Map<String, SseEmitter> emitters = emitterRepository
                .findAllEmitterStartWithByAuctionId(String.valueOf(bid.getAuction().getId()));
        // 데이터 캐시 저장 (유실된 데이터 처리 위함)
        emitterRepository.saveEventCache(eventId, bid.getPrice());
        emitters.forEach(
                (key, emitter) -> {
                    // 데이터 전송
                    sendToClient(emitter,"new price", key, eventId, bid.getPrice());
                }
        );

    }


}
