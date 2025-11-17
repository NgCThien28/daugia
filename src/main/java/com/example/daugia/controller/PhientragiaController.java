package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.service.PhientragiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @GetMapping("/find-all")
    public ApiResponse<List<BiddingDTO>> findAll() {
        List<BiddingDTO> list = phientragiaService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    // REST tạo trả giá (ví dụ Postman). Có thể bổ sung xác thực token nếu cần:
    @PostMapping("/create")
    public ApiResponse<BiddingDTO> createBidding(
            @RequestParam String maphienDauGia,
            @RequestParam String makh,
            @RequestParam int solan
    ) {
        BiddingDTO dto = phientragiaService.createBid(maphienDauGia, makh, solan);
        // Broadcast
        messagingTemplate.convertAndSend("/topic/auction/" + maphienDauGia, dto);
        return ApiResponse.success(dto, "Trả giá thành công");
    }

    // WebSocket endpoint: client gửi tới /app/bid (STOMP)
    @MessageMapping("/bid")
    public void handleBid(BiddingDTO incoming) {
        try {
            BiddingDTO dto = phientragiaService.createBid(
                    incoming.getPhienDauGia().getMaphiendg(),
                    incoming.getTaiKhoanNguoiRaGia().getMatk(),
                    incoming.getSolan()
            );
            messagingTemplate.convertAndSend("/topic/auction/" + dto.getPhienDauGia().getMaphiendg(), dto);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            messagingTemplate.convertAndSend("/topic/auction/" + incoming.getPhienDauGia().getMaphiendg(), error);
        }
    }
}
