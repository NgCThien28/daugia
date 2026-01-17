package com.example.daugia.service;

import com.example.daugia.config.PaymentConfig;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.dto.response.*;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.ConflictException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhientragiaRepository;
import com.example.daugia.repository.PhieuthanhtoanRepository;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.util.excel.BaseExport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    // TIM KIEM
    public List<PaymentDTO> findAll() {
        List<Phieuthanhtoan> phieuthanhtoanList = phieuthanhtoanRepository.findAll();
        return phieuthanhtoanList.stream()
                .map(phieuthanhtoan -> new PaymentDTO(
                        phieuthanhtoan.getMatt(),
                        new UserShortDTO(
                                phieuthanhtoan.getTaiKhoan().getMatk(),
                                phieuthanhtoan.getTaiKhoan().getHo(),
                                phieuthanhtoan.getTaiKhoan().getTenlot(),
                                phieuthanhtoan.getTaiKhoan().getTen()
                        ),
                        new AuctionDTO(
                                phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoan.getThoigianthanhtoan(),
                        phieuthanhtoan.getHanthanhtoan(),
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
                phieuthanhtoan.getHanthanhtoan(),
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
                        phieuthanhtoan.getHanthanhtoan(),
                        phieuthanhtoan.getTrangthai(),
                        phieuthanhtoan.getSotien()
                ))
                .toList();
    }

    public Optional<Phieuthanhtoan> findByPhienDauGia(String maphiendg) {
        return phieuthanhtoanRepository.findByPhienDauGia_Maphiendg(maphiendg);
    }

    public Page<PaymentDTO> findByUserAndStatus(String email, TrangThaiPhieuThanhToan status, String keyword, Pageable pageable) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        Specification<Phieuthanhtoan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter tài khoản và trạng thái
            predicates.add(cb.equal(root.get("taiKhoan").get("matk"), taikhoan.getMatk()));
            predicates.add(cb.equal(root.get("trangthai"), status));

            // keyword, tìm kiếm trong matt hoặc maphiendg (case-insensitive)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate mattPredicate = cb.like(cb.lower(root.get("matt")), keywordLower);
                Predicate maphiendgPredicate = cb.like(cb.lower(root.get("phienDauGia").get("maphiendg")), keywordLower);
                predicates.add(cb.or(mattPredicate, maphiendgPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Phieuthanhtoan> page = phieuthanhtoanRepository.findAll(spec, pageable);

        return page.map(p -> new PaymentDTO(
                p.getMatt(),
                new UserShortDTO(p.getTaiKhoan().getMatk()),
                new AuctionDTO(
                        p.getPhienDauGia().getMaphiendg(),
                        p.getPhienDauGia().getGiacaonhatdatduoc(),
                        new ProductDTO(p.getPhienDauGia().getSanPham().getTensp())
                ),
                p.getThoigianthanhtoan(),
                p.getHanthanhtoan(),
                p.getTrangthai(),
                p.getSotien()
        ));
    }

    // THANH TOAN
    // Tao phieu winner cu the
    public Phieuthanhtoan createForWinner(Phiendaugia phienDauGia, Taikhoan winner, BigDecimal giaThang) {
        Optional<Phieuthanhtoan> existing = phieuthanhtoanRepository.findByPhienDauGia_Maphiendg(phienDauGia.getMaphiendg());
        if (existing.isPresent()) {
            return existing.get();
        }

        Phieuthanhtoan phieu = new Phieuthanhtoan();
        phieu.setTaiKhoan(winner);
        phieu.setPhienDauGia(phienDauGia);
        phieu.setTrangthai(TrangThaiPhieuThanhToan.UNPAID);
        phieu.setSotien(giaThang.subtract(phienDauGia.getTiencoc()));
        Timestamp now = Timestamp.from(Instant.now());
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        phieu.setHanthanhtoan(new Timestamp(now.getTime() + sevenDaysMs));

        return phieuthanhtoanRepository.save(phieu);
    }

    // Goi winner tu bids
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

    public String createOrder(HttpServletRequest request) {
        Phieuthanhtoan phieu = phieuthanhtoanRepository.findById(request.getParameter("matt"))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        validatePhieuForPayment(phieu);

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

        String orderInfo = request.getParameter("vnp_OrderInfo");
        if (orderInfo == null || !orderInfo.contains("matt=")) {
            throw new ValidationException("Thiếu mã phiếu thanh toán trong OrderInfo.");
        }

        String matt = orderInfo.split("matt=")[1].split("&")[0];
        Phieuthanhtoan phieu = phieuthanhtoanRepository.findById(matt)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));

        String status = request.getParameter("vnp_TransactionStatus");

        if ("00".equals(status)) { // Thanh toán thành công
            validatePhieuForPayment(phieu);
            phieu.setTrangthai(TrangThaiPhieuThanhToan.PAID);
            phieu.setVnptransactionno(fields.get("vnp_TransactionNo"));
            phieu.setBankcode(fields.get("vnp_BankCode"));
            phieu.setThoigianthanhtoan(Timestamp.valueOf(LocalDateTime.now()));
            phieuthanhtoanRepository.save(phieu);
            return 1;
        } else {
            return 0;
        }
    }

    // HELPER
    private void validatePhieuForPayment(Phieuthanhtoan phieu) {
        if (phieu.getTrangthai().equals(TrangThaiPhieuThanhToan.PAID)) {
            throw new ConflictException("Phiếu đã được thanh toán");
        }
        if (!phieu.getHanthanhtoan().after(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("Đã quá thời hạn thanh toán");
        }
    }

    //Admin
    public PaymentDTO cancel(String matt) {
        Phieuthanhtoan ptt = phieuthanhtoanRepository.findById(matt)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));
        if (ptt.getTrangthai() != TrangThaiPhieuThanhToan.UNPAID)
            throw new ValidationException("Phiếu đã được thanh toán hoặc bị huỷ");
        ptt.setTrangthai(TrangThaiPhieuThanhToan.CANCELLED);
        phieuthanhtoanRepository.save(ptt);
        return new PaymentDTO(ptt.getMatt(),
                new UserShortDTO(
                        ptt.getTaiKhoan().getMatk(),
                        ptt.getTaiKhoan().getEmail()
                ),
                new AuctionDTO(
                        ptt.getPhienDauGia().getMaphiendg(),
                        ptt.getPhienDauGia().getGiacaonhatdatduoc()
                ),
                ptt.getThoigianthanhtoan(),
                ptt.getHanthanhtoan(),
                ptt.getTrangthai(),
                ptt.getSotien()
        );
    }

    public List<PaymentDTO> filter(
            LocalDate fromDate,
            LocalDate toDate,
            TrangThaiPhieuThanhToan status
    ) {
        Timestamp from = null;
        Timestamp to = null;

        if (fromDate != null) {
            from = Timestamp.valueOf(fromDate.atStartOfDay());
        }

        if (toDate != null) {
            to = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        }

        return phieuthanhtoanRepository.filter(from, to, status)
                .stream()
                .map(phieuthanhtoan -> new PaymentDTO(
                        phieuthanhtoan.getMatt(),
                        new UserShortDTO(
                                phieuthanhtoan.getTaiKhoan().getMatk(),
                                phieuthanhtoan.getTaiKhoan().getHo(),
                                phieuthanhtoan.getTaiKhoan().getTenlot(),
                                phieuthanhtoan.getTaiKhoan().getTen()
                        ),
                        new AuctionDTO(
                                phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoan.getThoigianthanhtoan(),
                        phieuthanhtoan.getHanthanhtoan(),
                        phieuthanhtoan.getTrangthai(),
                        phieuthanhtoan.getSotien()
                ))
                .toList();
    }

    public void export(LocalDate from,
                       LocalDate to,
                       TrangThaiPhieuThanhToan status,
                       HttpServletResponse response) throws IOException {
        List<PaymentDTO> listPTT = this.filter(from, to, status);

        BaseExport export = new BaseExport();

        // Sheet 1 - Thanh toán
        export.createSheet("Thanh toán");
        export.writeTitle(
                "THỐNG KÊ PHIẾU THANH TOÁN", 0, 0, 3
        );
        export.writeDateRangeLine(from, to, 1, 0, 3);
        export.writeHeader(
                new String[]{"Mã phiếu", "Ngày thanh toán", "Số tiền", "Trạng thái"},
                2
        ).writeData(
                listPTT,
                new String[]{"matt", "thoigianthanhtoan", "sotien", "trangthai"},
                PaymentDTO.class,
                3
        );
        // Freeze 3 dòng đầu (title + date line + header)
        export.freeze(0, 3);

        // AutoFilter cho header (row 2, A-D)
        export.applyAutoFilter(2, 0, 3);


        int dataStartRow = 3;

        if (!listPTT.isEmpty()) {
            int dataEndRow = dataStartRow + listPTT.size() - 1;
            int totalRow = dataEndRow + 1;

            // "Tổng tiền" đặt ở cột B (index 1), tổng ở cột C (index 2)
            export.writeTotalRow(totalRow, 1, 2, dataStartRow, dataEndRow);
        }

        export.export(response);
    }

    public List<TransactionDTO> getSuccessfulTransactions(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Timestamp from = null;
        Timestamp to = null;

        if (fromDate != null) {
            from = Timestamp.valueOf(fromDate.atStartOfDay());
        }

        if (toDate != null) {
            to = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        }
        List<Object[]> results = phieuthanhtoanRepository.
                findSuccessfulTransactions(from, to, TrangThaiPhieuThanhToan.PAID);
        return results.stream()
                .map(row -> new TransactionDTO((Date) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    public List<TransactionAmountDTO> getGrossMerchandiseValue(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Timestamp from = null;
        Timestamp to = null;

        if (fromDate != null) {
            from = Timestamp.valueOf(fromDate.atStartOfDay());
        }

        if (toDate != null) {
            to = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        }
        List<Object[]> results = phieuthanhtoanRepository.
                findGrossMerchandiseValue(from, to, TrangThaiPhieuThanhToan.PAID);
        return results.stream()
                .map(row -> new TransactionAmountDTO((Date) row[0], (BigDecimal) row[1]))
                .toList();
    }

    public List<TransactionAmountDTO> getCommission(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Timestamp from = null;
        Timestamp to = null;

        if (fromDate != null) {
            from = Timestamp.valueOf(fromDate.atStartOfDay());
        }

        if (toDate != null) {
            to = Timestamp.valueOf(toDate.atTime(23, 59, 59));
        }
        List<Object[]> results = phieuthanhtoanRepository.
                findCommission(from, to, TrangThaiPhieuThanhToan.PAID);
        return results.stream()
                .map(row -> new TransactionAmountDTO((Date) row[0], (BigDecimal) row[1]))
                .toList();
    }
}