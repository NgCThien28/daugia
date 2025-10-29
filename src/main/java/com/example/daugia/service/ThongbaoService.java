package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiThongBao;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.entity.Thongbao;
import com.example.daugia.exception.ForbiddenException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.TaikhoanquantriRepository;
import com.example.daugia.repository.ThongbaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class ThongbaoService {
    @Autowired
    private ThongbaoRepository thongbaoRepository;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private NotificationService notificationService;

    public List<NotificationDTO> findAll() {
        List<Thongbao> list = thongbaoRepository.findAll();
        return list.stream()
                .map(tb -> new NotificationDTO(
                        tb.getMatb(),
                        new UserShortDTO(tb.getTaiKhoanQuanTri().getMatk()),
                        new UserShortDTO(tb.getTaiKhoan().getMatk()),
                        tb.getNoidung(),
                        tb.getThoigian()
                ))
                .toList();
    }

    public Page<NotificationDTO> findByUser(String email, Pageable pageable) {
        Taikhoan user = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        Page<Thongbao> page = thongbaoRepository.findByTaiKhoan_Matk(user.getMatk(), pageable);
        long totalUnread = thongbaoRepository.findByTaiKhoan_Matk(user.getMatk())
                .stream()
                .filter(tb -> tb.getTrangthai() == TrangThaiThongBao.NOT_VIEWED)
                .count();
        return page.map(tb -> new NotificationDTO(
                tb.getMatb(),
                tb.getTieude(),
                tb.getNoidung(),
                tb.getThoigian(),
                tb.getTrangthai().getValue(),
                totalUnread
        ));
    }

    public void markAsRead(String matb, String email) {
        Thongbao tb = thongbaoRepository.findById(matb)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo"));;
        // Check ownership
        if (!tb.getTaiKhoan().getEmail().equals(email)) {
            throw new ForbiddenException("Không có quyền đánh dấu đã đọc thông báo này");
        }
        tb.setTrangthai(TrangThaiThongBao.VIEWED);
        thongbaoRepository.save(tb);;
    }

    @Transactional
    public void clearAllNotifications(String email) {
        Taikhoan user = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        thongbaoRepository.deleteByTaiKhoan_Matk(user.getMatk());
    }

    @Transactional
    public void markAllAsRead(String email) {
        Taikhoan user = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        thongbaoRepository.markAllAsReadByTaiKhoan_Matk(user.getMatk());
    }

    public void createForUser(ThongBaoCreationRequest request, String adminEmail, String userEmail) {
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        Taikhoan user = taikhoanRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản user"));

        Thongbao tb = new Thongbao();
        tb.setTaiKhoanQuanTri(admin);
        tb.setTaiKhoan(user);
        tb.setTieude(request.getTieude());
        tb.setNoidung(request.getNoidung());
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.NOT_VIEWED);
        Thongbao saved = thongbaoRepository.save(tb);
        // Push SSE
        notificationService.sendNotification(userEmail, saved);
    }

    public void createForUser(ThongBaoCreationRequest request, String userEmail) {
        Taikhoan user = taikhoanRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản user"));

        Thongbao tb = new Thongbao();
        tb.setTaiKhoan(user);
        tb.setTieude(request.getTieude());
        tb.setNoidung(request.getNoidung());
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.NOT_VIEWED);
        Thongbao saved = thongbaoRepository.save(tb);
        // Push SSE
        notificationService.sendNotification(userEmail, saved);
    }

    public void createForUser(String mapdg, String adminEmail, String userEmail) {
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        Taikhoan user = taikhoanRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản user"));

        Thongbao tb = new Thongbao();
        tb.setTaiKhoanQuanTri(admin);
        tb.setTaiKhoan(user);
        tb.setTieude("Chúc mừng phiên đã duyệt");
        tb.setNoidung("Phiên đấu giá của khách hàng có mã " + mapdg + " đã được duyệt!");
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.NOT_VIEWED);
        Thongbao saved = thongbaoRepository.save(tb);
        // Push SSE
        notificationService.sendNotification(userEmail, saved);
    }

    public void createForProductToUser(String masp, String adminEmail, String userEmail) {
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        Taikhoan user = taikhoanRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản user"));

        Thongbao tb = new Thongbao();
        tb.setTaiKhoanQuanTri(admin);
        tb.setTaiKhoan(user);
        tb.setTieude("Chúc mừng tài sản đã được duyệt");
        tb.setNoidung("Tài sản của khách hàng có mã " + masp + " đã được duyệt!");
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.NOT_VIEWED);
        Thongbao saved = thongbaoRepository.save(tb);
        // Push SSE
        notificationService.sendNotification(userEmail, saved);
    }

    public Thongbao update() {
        Thongbao tb = new Thongbao();
        return tb;
    }
}
