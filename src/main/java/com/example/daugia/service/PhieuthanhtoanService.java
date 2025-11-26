package com.example.daugia.service;

import com.example.daugia.config.PaymentConfig;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.*;
import com.example.daugia.exception.ConflictException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhieuthanhtoanRepository;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhientragiaRepository;
import com.example.daugia.repository.TaikhoanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.*;

@Service
public class PhieuthanhtoanService {

    @Autowired
    private PhieuthanhtoanRepository phieuthanhtoanRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private PhientragiaRepository phientragiaRepository;

    public List<PaymentDTO> findAll() {
        List<Phieuthanhtoan> phieuthanhtoanList = phieuthanhtoanRepository.findAll();
        return phieuthanhtoanList.stream()
                .map(phieuthanhtoan -> new PaymentDTO(
                        phieuthanhtoan.getMatt(),
                        new UserShortDTO(phieuthanhtoan.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoan.getThoigianthanhtoan(),
                        phieuthanhtoan.getTrangthai(),
                        phieuthanhtoan.getSotien()
                ))
                .toList();
    }

    public PaymentDTO findById(String id) {
        Phieuthanhtoan phieuthanhtoan = phieuthanhtoanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));
        return new PaymentDTO(
                phieuthanhtoan.getMatt(),
                new UserShortDTO(phieuthanhtoan.getTaiKhoan().getMatk()),
                new AuctionDTO(
                        phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                        phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                ),
                phieuthanhtoan.getThoigianthanhtoan(),
                phieuthanhtoan.getTrangthai(),
                phieuthanhtoan.getSotien()
        );
    }

    public List<PaymentDTO> findByUser(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        List<Phieuthanhtoan> phieuthanhtoanList = phieuthanhtoanRepository.findByTaiKhoan_Matk(taikhoan.getMatk());
        return phieuthanhtoanList.stream()
                .map(phieuthanhtoan -> new PaymentDTO(
                        phieuthanhtoan.getMatt(),
                        new UserShortDTO(phieuthanhtoan.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoan.getThoigianthanhtoan(),
                        phieuthanhtoan.getTrangthai(),
                        phieuthanhtoan.getSotien()
                ))
                .toList();
    }

    // Thêm method tạo phiếu cho winner cụ thể (cho fallback)
    public Phieuthanhtoan createForWinner(Phiendaugia phienDauGia, Taikhoan winner, BigDecimal giaThang) {
        // Kiểm tra đã có phiếu chưa
        Optional<Phieuthanhtoan> existing = phieuthanhtoanRepository.findByPhienDauGia_Maphiendg(phienDauGia.getMaphiendg());
        if (existing.isPresent()) {
            return existing.get();
        }

        Phieuthanhtoan phieu = new Phieuthanhtoan();
        phieu.setTaiKhoan(winner);
        phieu.setPhienDauGia(phienDauGia);
        phieu.setTrangthai(TrangThaiPhieuThanhToan.UNPAID);
        phieu.setSotien(giaThang);
        // Thời gian thanh toán: 7 ngày từ bây giờ
        Timestamp now = Timestamp.from(Instant.now());
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        phieu.setThoigianthanhtoan(new Timestamp(now.getTime() + sevenDaysMs));

        return phieuthanhtoanRepository.save(phieu);
    }

    // Giữ method cũ, gọi với winner từ bids
    public Phieuthanhtoan createForWinner(Phiendaugia phienDauGia) {
        List<Phientragia> bids = phientragiaRepository.findByPhienDauGia_Maphiendg(phienDauGia.getMaphiendg());
        if (bids.isEmpty()) {
            throw new ValidationException("Không có người tham gia trả giá để tạo phiếu thanh toán");
        }

        Phientragia winnerBid = bids.stream()
                .max(Comparator.comparing(Phientragia::getSotien))
                .orElseThrow(() -> new ValidationException("Không tìm thấy người thắng"));

        return createForWinner(phienDauGia, winnerBid.getTaiKhoan(), winnerBid.getSotien());
    }

    public Optional<Phieuthanhtoan> findByPhienDauGia(String maphiendg) {
        return phieuthanhtoanRepository.findByPhienDauGia_Maphiendg(maphiendg);
    }

    public String createOrder(HttpServletRequest request) {
        Phieuthanhtoan phieu = phieuthanhtoanRepository.findById(request.getParameter("matt"))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        Timestamp thoigianThanhToanChoPhep = phieu.getThoigianthanhtoan();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Chỉ tạo order nếu chưa quá hạn và chưa thanh toán
        if (phieu.getTrangthai().equals(TrangThaiPhieuThanhToan.PAID)) {
            throw new ConflictException("Phiếu đã được thanh toán.");
        }
        if (!thoigianThanhToanChoPhep.after(now)) {
            throw new ValidationException("Đã quá thời hạn thanh toán.");
        }

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = PaymentConfig.getRandomNumber(8);
        String vnp_IpAddr = PaymentConfig.getIpAddress(request);
        String vnp_TmnCode = PaymentConfig.vnp_TmnCode;
        String orderType = "other";
        long amount;
        try {
            amount = Integer.parseInt(request.getParameter("amount"));
        } catch (NumberFormatException ex) {
            throw new ValidationException("Số tiền không hợp lệ");
        }
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan thang phien matt=" + request.getParameter("matt"));
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", PaymentConfig.vnp_ReturnPaymentUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = PaymentConfig.hmacSHA512(PaymentConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return PaymentConfig.vnp_PayUrl + "?" + queryUrl;
    }

    @Transactional
    public int orderReturn(HttpServletRequest request) throws JsonProcessingException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = PaymentConfig.hashAllFields(fields);
        if (!signValue.equals(vnp_SecureHash)) {
            System.err.println("[VNPAY] Invalid signature!");
            return -1;
        }

        // Lấy mã phiếu từ vnp_OrderInfo
        String orderInfo = request.getParameter("vnp_OrderInfo");
        if (orderInfo == null || !orderInfo.contains("matt=")) {
            throw new ValidationException("Thiếu mã phiếu thanh toán trong OrderInfo.");
        }

        String matt = orderInfo.split("matt=")[1].split("&")[0];
        Phieuthanhtoan phieu = phieuthanhtoanRepository.findById(matt)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        String status = request.getParameter("vnp_TransactionStatus");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp thoigianThanhToanChoPhep = phieu.getThoigianthanhtoan();

        if ("00".equals(status)) { // Thanh toán thành công
            if (!phieu.getTrangthai().equals(TrangThaiPhieuThanhToan.PAID)) {
                if (thoigianThanhToanChoPhep.after(now)) {
                    phieu.setTrangthai(TrangThaiPhieuThanhToan.PAID);
                    phieu.setVnptransactionno(fields.get("vnp_TransactionNo"));
                    phieu.setBankcode(fields.get("vnp_BankCode"));
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String rawJson = objectMapper.writeValueAsString(fields);
                        phieu.setRaw(rawJson);
                    } catch (JsonProcessingException ignore) {
                    }
                    phieuthanhtoanRepository.save(phieu);
                    return 1;
                } else {
                    throw new ValidationException("Đã quá thời hạn thanh toán");
                }
            } else {
                throw new ConflictException("Phiếu đã được thanh toán trước đó");
            }
        } else {
            return 0; // Thanh toán thất bại hoặc bị hủy
        }
    }

    // Ví dụ:
    public Page<PaymentDTO> findByUserAndStatusPaged(String email, TrangThaiPhieuThanhToan status, Pageable pageable) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        Page<Phieuthanhtoan> page = phieuthanhtoanRepository
                .findByTaiKhoan_MatkAndTrangthai(taikhoan.getMatk(), status, pageable);
        return page.map(p -> new PaymentDTO(
                p.getMatt(),
                new UserShortDTO(p.getTaiKhoan().getMatk()),
                new AuctionDTO(
                        p.getPhienDauGia().getMaphiendg(),
                        p.getPhienDauGia().getGiacaonhatdatduoc()
                ),
                p.getThoigianthanhtoan(),
                p.getTrangthai(),
                p.getSotien()
        ));
    }
}