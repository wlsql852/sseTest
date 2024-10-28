package com.sparta.ssetest.service;

import com.sparta.ssetest.dto.NotificationRequestDto;
import com.sparta.ssetest.entity.Notification;
import com.sparta.ssetest.repository.EmitterRepositoryImpl;
import com.sparta.ssetest.entity.User;
import com.sparta.ssetest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

public interface NotificationService {

    // 게시글에 좋아요 버튼 클릭 시
    void notifyToUsersThatTheyHaveReceivedLike(PostLike postLike);
//    // 게시글에 댓글 작성 시
//    void notifyToUsersThatTheyHaveReceivedComment(PostComment postComment);
//    // 중고거래 게시글에 좋아요 버튼 클릭 시
//    void notifyToUsersThatTheyHaveReceivedLike(TradeLike tradeLike);
//    // 중고거래 게시글에 댓글 작성 시
//    void notifyToUsersThatTheyHaveReceivedComment(TradeComment tradeComment);
//    // 중고상품 구매 요청 시
//    void notifyToSellersThatTheyHaveReceivedTradeChatRequest(TradeChatRoom tradeChatRoom);
    SseEmitter subscribe(User user, String lastEventId);
    void send(NotificationRequestDto request);
    void sendToClient(SseEmitter emitter, String emitterId, String eventId, Object data);
//    NotificationListResponseDto getNotifications(User user);
//    void readNotification(Long notificationId, User user);
//    void deleteNotification(Long notificationId, User user);
//    Notification findNotification(Long notificationId);
}
