package com.example.daugia.service;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class NotificationService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String email) {
        // Nếu emitter cũ còn tồn tại -> hủy trước
        SseEmitter oldEmitter = emitters.remove(email);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
            } catch (Exception ignored) {}
        }

        SseEmitter emitter = new SseEmitter(0L); // 0L = không timeout
        emitters.put(email, emitter);

        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));
        emitter.onError((ex) -> emitters.remove(email));

        return emitter;
    }

    public void sendLogoutEvent(String email, boolean isSelfLogout) {
        SseEmitter emitter = emitters.get(email);
        if (emitter != null) {
            try {
                if (isSelfLogout) {
                    emitter.send(SseEmitter.event()
                            .name("self-logout")
                            .data("Đăng xuất thành công!"));
                } else {
                    emitter.send(SseEmitter.event()
                            .name("force-logout")
                            .data("Tài khoản của bạn đã đăng nhập ở nơi khác"));
                }
                emitter.complete();
                emitters.remove(email);
            } catch (Exception e) {
                emitters.remove(email);
            }
        }
    }

    public void sendBanEvent(String email) {
        SseEmitter emitter = emitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("banned")
                        .data("Tài khoản của bạn đã bị khoá!"));
                emitter.complete();
                emitters.remove(email);
            } catch (Exception e) {
                emitters.remove(email);
            }
        }
    }
}

