package com.example.daugia.service;

import com.example.daugia.config.PaymentConfig;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.ConflictException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import com.example.daugia.repository.TaikhoanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
public class PhieuthanhtoantiencocService {
    @Autowired
    private PhieuthanhtoantiencocRepository phieuthanhtoantiencocRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;

    public List<DepositDTO> findAll() {
        List<Phieuthanhtoantiencoc> phieuthanhtoantiencocList = phieuthanhtoantiencocRepository.findAll();
        return phieuthanhtoantiencocList.stream()
                .map(phieuthanhtoantiencoc -> new DepositDTO(
                        phieuthanhtoantiencoc.getMatc(),
                        new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoantiencoc.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoantiencoc.getThoigianthanhtoan(),
                        phieuthanhtoantiencoc.getTrangthai()
                ))
                .toList();
    }

    public DepositDTO findById(String id) {
        Phieuthanhtoantiencoc phieuthanhtoantiencoc = phieuthanhtoantiencocRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy"));
        DepositDTO depositDTO = new DepositDTO();
        depositDTO.setMatc(phieuthanhtoantiencoc.getMatc());
        depositDTO.setTrangthai(phieuthanhtoantiencoc.getTrangthai());
        depositDTO.setPhienDauGia(new AuctionDTO(phieuthanhtoantiencoc.getPhienDauGia().getTiencoc(), phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg()));
        depositDTO.setTaiKhoanKhachThanhToan(new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()));
        return depositDTO;
    }

    public List<DepositDTO> findByUser(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        List<Phieuthanhtoantiencoc> phieuthanhtoantiencocList = phieuthanhtoantiencocRepository.findByTaiKhoan_Matk(taikhoan.getMatk());
        return phieuthanhtoantiencocList.stream()
                .map(phieuthanhtoantiencoc -> new DepositDTO(
                        phieuthanhtoantiencoc.getMatc(),
                        new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoantiencoc.getPhienDauGia().getTiencoc(),
                                phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg()
                        ),
                        phieuthanhtoantiencoc.getThoigianthanhtoan(),
                        phieuthanhtoantiencoc.getTrangthai()
                ))
                .toList();
    }

    public DepositDTO create(PhieuthanhtoantiencocCreationRequest request, String email) {

        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        if (taikhoan.getXacthuctaikhoan() == TrangThaiTaiKhoan.INACTIVE) {
            throw new ValidationException("Tài khoản chưa được xác thực, vui lòng xác thực email trước khi tham gia đấu giá");
        }
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(request.getMaphien())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));

        if (phiendaugia.getTaiKhoan().getMatk().equals(taikhoan.getMatk())) {
            throw new ValidationException("Bạn không thể đăng ký tham gia phiên đấu giá do chính mình tạo ra");
        }

        Optional<Phieuthanhtoantiencoc> existing =
                phieuthanhtoantiencocRepository.findByTaiKhoan_MatkAndPhienDauGia_Maphiendg(
                        taikhoan.getMatk(), phiendaugia.getMaphiendg()
                );

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Đã đăng ký phiên đấu giá này");
        }
        Phieuthanhtoantiencoc phieuthanhtoantiencoc = new Phieuthanhtoantiencoc();
        phieuthanhtoantiencoc.setTaiKhoan(taikhoan);
        phieuthanhtoantiencoc.setPhienDauGia(phiendaugia);
        Timestamp now = Timestamp.from(Instant.now());
        Timestamp thoigianktdk = phiendaugia.getThoigianktdk();

        if (now.after(thoigianktdk)) {
            throw new ValidationException("Đã quá thời hạn đăng ký");
        }

        // Tính thời gian thanh toán tối đa 7 ngày
        long maxPaymentMillis = 7L * 24 * 60 * 60 * 1000; // 7 ngày
        long remainingMillis = thoigianktdk.getTime() - now.getTime();
        long paymentMillis = Math.min(maxPaymentMillis, remainingMillis);

        phieuthanhtoantiencoc.setThoigianthanhtoan(new Timestamp(now.getTime() + paymentMillis));
        phieuthanhtoantiencoc.setTrangthai(TrangThaiPhieuThanhToanTienCoc.UNPAID);
        phieuthanhtoantiencocRepository.save(phieuthanhtoantiencoc);
        return findById(phieuthanhtoantiencoc.getMatc());
    }

    public String createOrder(HttpServletRequest request) {
        Phieuthanhtoantiencoc phieu = phieuthanhtoantiencocRepository.findById(request.getParameter("matc"))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        Timestamp thoigianThanhToanChoPhep = phieu.getThoigianthanhtoan();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Chỉ tạo order nếu chưa quá hạn và chưa thanh toán
        if (phieu.getTrangthai().equals(TrangThaiPhieuThanhToanTienCoc.PAID)) {
            throw new ConflictException("Phiếu đã được thanh toán");
        }
        if (!thoigianThanhToanChoPhep.after(now)) {
            throw new ValidationException("Đã quá thời hạn thanh toán");
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
        vnp_Params.put("vnp_OrderInfo", "Thanh toan tien coc matc=" + request.getParameter("matc"));
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", PaymentConfig.vnp_ReturnDepositUrl);
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
        if (orderInfo == null || !orderInfo.contains("matc=")) {
            throw new ValidationException("Thiếu mã phiếu thanh toán");
        }

        String matc = orderInfo.split("matc=")[1].split("&")[0];
        Phieuthanhtoantiencoc phieu = phieuthanhtoantiencocRepository.findById(matc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        String status = request.getParameter("vnp_TransactionStatus");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp thoigianThanhToanChoPhep = phieu.getThoigianthanhtoan();

        if ("00".equals(status)) { // Thanh toán thành công
            if (!phieu.getTrangthai().equals(TrangThaiPhieuThanhToanTienCoc.PAID)) {
                if (thoigianThanhToanChoPhep.after(now)) {
                    phieu.setTrangthai(TrangThaiPhieuThanhToanTienCoc.PAID);
                    phieu.setVnptransactionno(fields.get("vnp_TransactionNo"));
                    phieu.setBankcode(fields.get("vnp_BankCode"));
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String rawJson = objectMapper.writeValueAsString(fields);
                        phieu.setRaw(rawJson);
                    } catch (JsonProcessingException ignore) {
                    }
                    Phiendaugia phien = phieu.getPhienDauGia();
                    phien.setSlnguoithamgia(phien.getSlnguoithamgia() + 1);
                    phiendaugiaRepository.save(phien);
                    phieuthanhtoantiencocRepository.save(phieu);
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

    public Page<DepositDTO> findByAccountAndStatusPaged(String matk,
                                                        TrangThaiPhieuThanhToanTienCoc status,
                                                        Pageable pageable) {
        Page<Phieuthanhtoantiencoc> page = phieuthanhtoantiencocRepository
                .findByTaiKhoan_MatkAndTrangthai(matk, status, pageable);

        // Map Page<Entity> -> Page<DTO> bằng Page.map(...)
        return page.map(p -> new DepositDTO(
                p.getMatc(),
                new UserShortDTO(p.getTaiKhoan().getMatk()),
                new AuctionDTO(
                        p.getPhienDauGia().getMaphiendg(),
                        p.getPhienDauGia().getGiacaonhatdatduoc()
                ),
                p.getThoigianthanhtoan(),
                p.getTrangthai()
        ));
    }

    // Thêm method mới
    public Page<DepositDTO> findByUserAndStatusPaged(String email, TrangThaiPhieuThanhToanTienCoc status, Pageable pageable) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        Page<Phieuthanhtoantiencoc> page = phieuthanhtoantiencocRepository
                .findByTaiKhoan_MatkAndTrangthai(taikhoan.getMatk(), status, pageable);
        return page.map(p -> new DepositDTO(
                p.getMatc(),
                new UserShortDTO(p.getTaiKhoan().getMatk()),
                new AuctionDTO(
                        p.getPhienDauGia().getTiencoc(),
                        p.getPhienDauGia().getMaphiendg()
                ),
                p.getThoigianthanhtoan(),
                p.getTrangthai()
        ));
    }

}
