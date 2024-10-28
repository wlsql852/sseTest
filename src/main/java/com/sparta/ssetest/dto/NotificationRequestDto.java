package com.sparta.ssetest.dto;

import com.sparta.ssetest.entity.Notification;
import com.sparta.ssetest.entity.NotificationType;
import com.sparta.ssetest.entity.User;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class NotificationRequestDto {

    private String url;
    private String content;
    private User receiver;
    private NotificationType notificationType;

    public Notification toEntity(NotificationType notificationType, User receiver) {
        return Notification.builder()
                .url(this.url)
                .content(this.content)
                .notificationType(notificationType)
                .receiver(receiver)
                .isRead(false)
                .build();
    }


}
