package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.ws.HistoryCache;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.service.PhientragiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/biddings")
public class PhientragiaController {

    @Autowired
    private PhientragiaService phientragiaService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private HistoryCache historyCache;

    @PostMapping("/create")
    public ApiResponse<BiddingDTO> createBidding(
            @RequestParam String maphienDauGia,
            @RequestParam String makh,
            @RequestParam int solan
    ) {
        BiddingDTO dto = phientragiaService.createBid(maphienDauGia, makh, solan);
        historyCache.append(maphienDauGia, dto);
        messagingTemplate.convertAndSend("/topic/auction/" + maphienDauGia, dto);
        return ApiResponse.success(dto, "Trả giá thành công");
    }

    @MessageMapping("/bid")
    public void handleBid(BiddingDTO incoming) {
        try {
            BiddingDTO dto = phientragiaService.createBid(
                    incoming.getPhienDauGia().getMaphiendg(),
                    incoming.getTaiKhoanNguoiRaGia().getMatk(),
                    incoming.getSolan()
            );
            historyCache.append(dto.getPhienDauGia().getMaphiendg(), dto);
            messagingTemplate.convertAndSend("/topic/auction/" + dto.getPhienDauGia().getMaphiendg(), dto);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            messagingTemplate.convertAndSend("/topic/auction/" + incoming.getPhienDauGia().getMaphiendg(), error);
        }
    }

    public static record HistoryRequest(String maphienDauGia, Integer limit) {}

    // Trả lịch sử trực tiếp cho session đã gửi /app/history
    @MessageMapping("/history")
    @SendToUser("/queue/history")
    public List<BiddingDTO> history(HistoryRequest req) {
        String id = req.maphienDauGia();
        int limit = (req.limit() == null ? 20 : req.limit());
        if (id == null || id.isBlank()) return List.of();
        return historyCache.getLast(id, limit);
    }
}