package com.example.daugia.service;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Thongbao;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class NotificationService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> auctionEmitters = new ConcurrentHashMap<>();
    public SseEmitter createEmitter(String email) {
        // Nếu emitter cũ còn tồn tại thi huy
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

    public SseEmitter createAuctionEmitter(String maphiendg) {
        SseEmitter oldEmitter = auctionEmitters.remove(maphiendg);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
            } catch (Exception ignored) {}
        }

        SseEmitter emitter = new SseEmitter(0L);
        auctionEmitters.put(maphiendg, emitter);

        emitter.onCompletion(() -> auctionEmitters.remove(maphiendg));
        emitter.onTimeout(() -> auctionEmitters.remove(maphiendg));
        emitter.onError((ex) -> auctionEmitters.remove(maphiendg));

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

    public void sendNotification(String email, Thongbao thongbao) {
        SseEmitter emitter = emitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(new NotificationDTO(
                                thongbao.getMatb(),
                                thongbao.getTieude(),
                                thongbao.getNoidung(),
                                thongbao.getThoigian(),
                                thongbao.getTrangthai().getValue()
                        )));
            } catch (Exception e) {
                emitters.remove(email);
            }
        }
    }

    public void sendNumberOfParticipants(Phiendaugia phiendaugia) {
        SseEmitter emitter = auctionEmitters.get(phiendaugia.getMaphiendg());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("number-of-participants")
                        .data(phiendaugia.getSlnguoithamgia()));
            } catch (Exception e) {
                auctionEmitters.remove(phiendaugia.getMaphiendg());
            }
        }
    }
}

