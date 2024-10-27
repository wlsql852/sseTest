package com.sparta.ssetest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService extends SseEmitter {

    private final UserRepository userRepository;
    private final EmitterRepositoryImpl emitterRepository;

    // subscribe 로 연결 요청 시 SseEmitter(발신기)를 생성합니다.
    public SseEmitter subscribe(Long userId, String lastEventId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new NullPointerException("해당 유저는 존재하지 암습니다"));
        String emitterId = makeTimeIncludeId(user);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // SseEmitter 의 완료/시간초과/에러로 인한 전송 불가 시 sseEmitter 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 클라이언트는 SSE Timeout 될 경우 자동으로 재연결 시도
        // 재연결 시 한 번도 데이터를 전송한 적이 없다면 503 에러가 발생하므로 최초 연결 시 더미 이벤트를 전송
        String eventId = makeTimeIncludeId(user);
        sendToClient(emitter, emitterId, eventId,
                "연결되었습니다. EventStream Created. [userId=" + user.getId() + "]");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        // 클라이언트의 요청 헤더에 lastEventId 값이 있는 경우 lastEventId 보다 더 큰(더 나중에 생성된) emitter를 찾아서 발송
        if (!lastEventId.isEmpty()) { // Last-Event-ID가 존재한다는 것은 받지 못한 데이터가 있다는 것이다. (프론트에서 알아서 보내준다.)
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithUserId(
                    String.valueOf(user.getId()));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getKey(),
                            entry.getValue()));
        }
        return emitter;
    }

    private String makeTimeIncludeId(User user) {  // 데이터 유실 시점 파악 위함
        return user.getId() + "_" + System.currentTimeMillis();
    }

    // 특정 SseEmitter 를 이용해 알림을 보냅니다. SseEmitter 는 최초 연결 시 생성되며,
    // 해당 SseEmitter 를 생성한 클라이언트로 알림을 발송하게 됩니다.
    public void sendToClient(SseEmitter emitter, String emitterId, String eventId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name("sse")
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            throw new IllegalArgumentException("sse가 연결되지 않았습니다");
        }
    }

    @Override
    public void send(NotificationRequestDto requestDto) {
        sendNotification(requestDto, saveNotification(requestDto));
    }
    // 알람 저장
    private Notification saveNotification(NotificationRequestDto requestDto) {
        Notification notification = Notification.builder()
                .receiver(requestDto.getReceiver())
                .notificationType(requestDto.getNotificationType())
                .content(requestDto.getContent())
                .url(requestDto.getUrl())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        return notification;
    }
}
