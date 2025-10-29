package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.service.PhientragiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;

@RestController
@RequestMapping("/biddings")
public class PhientragiaController {
    @Autowired
    private PhientragiaService phientragiaService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/find-all")
    public ApiResponse<List<BiddingDTO>> findAll(){
        ApiResponse<List<BiddingDTO>> apiResponse = new ApiResponse<>();
        try{
            List<BiddingDTO> phientragiaList = phientragiaService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(phientragiaList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    // Dành cho REST testing (Postman)
    @PostMapping("/create")
    public ApiResponse<BiddingDTO> createBidding(
            @RequestParam String maphienDauGia,
            @RequestParam String makh,
            @RequestParam int solan
    ) {
        ApiResponse<BiddingDTO> res = new ApiResponse<>();
        try {
            BiddingDTO dto = phientragiaService.createBid(maphienDauGia, makh, solan);

            // Broadcast realtime tới tất cả client đang theo dõi phiên này
            messagingTemplate.convertAndSend("/topic/auction/" + maphienDauGia, dto);
            res.setCode(200);
            res.setMessage("Trả giá thành công");
            res.setResult(dto);
        } catch (IllegalArgumentException e) {
            res.setCode(401);
            res.setMessage(e.getMessage());
        }
        return res;
    }

    // Dành cho WebSocket realtime
    @MessageMapping("/bid") // client gửi tới /app/bid
    public void handleBid(BiddingDTO bidding) {
        BiddingDTO dto = phientragiaService.createBid(
                bidding.getPhienDauGia().getMaphiendg(),
                bidding.getTaiKhoanNguoiRaGia().getMatk(),
                bidding.getSolan()
        );

        messagingTemplate.convertAndSend("/topic/auction/" + dto.getPhienDauGia().getMaphiendg(), dto);
    }

}
