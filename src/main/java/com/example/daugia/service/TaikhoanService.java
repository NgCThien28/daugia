package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.StorageException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.TaikhoanquantriRepository;
import com.example.daugia.repository.ThanhphoRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaikhoanService {
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private ThanhphoRepository thanhphoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;

    public Page<UserShortDTO> findAll(Pageable pageable) {
        Page<Taikhoan> taikhoanList = taikhoanRepository.findAll(pageable);
        return taikhoanList
                .map(taikhoan -> new UserShortDTO(
                        taikhoan.getMatk(),
                        taikhoan.getHo(),
                        taikhoan.getTenlot(),
                        taikhoan.getTen(),
                        taikhoan.getXacthuctaikhoan(),
                        taikhoan.getTrangthaidangnhap()
                ));
    }

    public Taikhoan findById(String id) {
        return taikhoanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    public Taikhoan findByEmail(String email) {
        String norm = normalizeEmail(email);
        return taikhoanRepository.findByEmail(norm)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    public Taikhoan createUser(TaikhoanCreationRequest request) throws MessagingException, IOException {
        String email = normalizeEmail(request.getEmail());
        if (taikhoanRepository.existsByEmail(email) || taikhoanquantriRepository.existsByEmail(email)) {
            throw new ValidationException("Email đã được sử dụng");
        }
        Taikhoan tk = new Taikhoan();
        tk.setHo(request.getHo());
        tk.setTenlot(request.getTenlot());
        tk.setTen(request.getTen());
        tk.setEmail(email);
        tk.setSdt(request.getSdt());
        tk.setMatkhau(passwordEncoder.encode(request.getMatkhau()));
        tk.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        tk.setXacthuctaikhoan(TrangThaiTaiKhoan.INACTIVE);
        tk.setXacthuckyc(TrangThaiTaiKhoan.INACTIVE);

        String token = UUID.randomUUID().toString();
        tk.setTokenxacthuc(token);
        tk.setTokenhethan(new Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000));

        Taikhoan saved = taikhoanRepository.save(tk);
        emailService.sendVerificationEmail(saved, token);

        saved.setMatkhau(null);
        return saved;
    }

    public boolean verifyUser(String token) {
        Taikhoan user = taikhoanRepository.findByTokenxacthuc(token)
                .orElseThrow(() -> new ValidationException("Token không hợp lệ"));

        if (user.getTokenhethan() != null &&
                user.getTokenhethan().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("Token đã hết hạn");
        }

        user.setXacthuctaikhoan(TrangThaiTaiKhoan.ACTIVE);
        user.setTokenxacthuc(null);
        user.setTokenhethan(null);
        taikhoanRepository.save(user);
        return true;
    }

    public void resendVerificationEmail(String email) throws MessagingException, IOException {
        Taikhoan user = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Email không tồn tại"));

        if (user.getXacthuctaikhoan().equals(TrangThaiTaiKhoan.ACTIVE)) {
            throw new ValidationException("Tài khoản đã được xác thực");
        }

        // Sinh token moi va cap nhat
        String newToken = UUID.randomUUID().toString();
        user.setTokenxacthuc(newToken);
        user.setTokenhethan(new Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000));
        taikhoanRepository.save(user);

        emailService.sendVerificationEmail(user, newToken);
    }

    public Taikhoan login(String email, String rawPassword) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Email không tồn tại"));

        if (!passwordEncoder.matches(rawPassword, taikhoan.getMatkhau())) {
            throw new ValidationException("Mật khẩu không đúng");
        }

        if (taikhoan.getTrangthaidangnhap().equals(TrangThaiTaiKhoan.BANNED)){
             throw new ValidationException("Tài khoản hiện đã bị khoá");
        }

        if (taikhoan.getXacthuctaikhoan().equals(TrangThaiTaiKhoan.INACTIVE)) {
            throw new ValidationException("Tài khoản hiện chưa xác thực email");
        }

        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.ONLINE);
        taikhoanRepository.save(taikhoan);

        taikhoan.setMatkhau(null);
        return taikhoan;
    }

    public void logout(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        taikhoanRepository.save(taikhoan);
    }

    public Taikhoan updateInfo(TaikhoanCreationRequest request, String email, List<MultipartFile> files) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        taikhoan.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Thành phố không tồn tại")));

        // Update cac field thuong
        taikhoan.setHo(request.getHo());
        taikhoan.setTenlot(request.getTenlot());
        taikhoan.setTen(request.getTen());
        taikhoan.setDiachi(request.getDiachi());
        taikhoan.setDiachigiaohang(request.getDiachigiaohang());
        taikhoan.setSdt(request.getSdt());

        boolean hasFront = taikhoan.getAnhmattruoc() != null && !taikhoan.getAnhmattruoc().isBlank();
        boolean hasBack  = taikhoan.getAnhmatsau() != null && !taikhoan.getAnhmatsau().isBlank();
        boolean hasKycImages = hasFront && hasBack;

        // null / rong / toan file empty => coi như không gui anh
        List<MultipartFile> safeFiles = (files == null) ? List.of() : files;
        List<MultipartFile> validFiles = safeFiles.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        boolean wantsUpdateImages = !validFiles.isEmpty();
        boolean kycActive = taikhoan.getXacthuckyc() == TrangThaiTaiKhoan.ACTIVE; // chỉnh theo enum thật của mày

        // Nhanh 4: da xac thuc KYC
        if (kycActive) {
            if (wantsUpdateImages) {
                throw new ValidationException("Tài khoản đã xác thực KYC, không được thay đổi ảnh CCCD");
            }
            Taikhoan saved = taikhoanRepository.save(taikhoan);
            saved.setMatkhau(null);
            return saved;
        }

        // KYC INACTIVE

        // Nhanh 1: cap nhat lan dau, chua co anh ma lai muon luu anh => bat buoc đu 2 anh
        if (!hasKycImages && wantsUpdateImages) {
            validateTwoImages(validFiles);
            List<String> added = internalAppend(validFiles);
            taikhoan.setAnhmattruoc(added.get(0));
            taikhoan.setAnhmatsau(added.get(1));

            Taikhoan saved = taikhoanRepository.save(taikhoan);
            saved.setMatkhau(null);
            return saved;
        }

        // Nhanh 2: chua xac thuc, đa co anh, chi update thuong (khong gui anh)
        if (hasKycImages && !wantsUpdateImages) {
            Taikhoan saved = taikhoanRepository.save(taikhoan);
            saved.setMatkhau(null);
            return saved;
        }

        // Nhanh 3: chua xac thuc, đa co anh, update ca anh (gui anh moi)
        if (hasKycImages && wantsUpdateImages) {
            validateTwoImages(validFiles);
            List<String> added = internalAppend(validFiles);
            taikhoan.setAnhmattruoc(added.get(0));
            taikhoan.setAnhmatsau(added.get(1));

            Taikhoan saved = taikhoanRepository.save(taikhoan);
            saved.setMatkhau(null);
            return saved;
        }

        // Truong hop con lai: chua xac thuc, chua co anh, nhung lai khong gui anh => khong hop le
        if (!hasKycImages && !wantsUpdateImages) {
            throw new ValidationException("Vui lòng tải lên đủ 2 ảnh CCCD (mặt trước & mặt sau)");
        }

        // fallback
        Taikhoan saved = taikhoanRepository.save(taikhoan);
        saved.setMatkhau(null);
        return saved;
    }

    private void validateTwoImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("Không có file nào được gửi");
        }
        if (files.size() != 2) {
            throw new ValidationException("Vui lòng gửi đúng 2 ảnh: mặt trước và mặt sau");
        }
        if (files.size() > 2) {
            throw new ValidationException("Tối đa 2 ảnh");
        }
    }


    public void changePassword(TaiKhoanChangePasswordRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getMatkhaucu(), taikhoan.getMatkhau())) {
            throw new ValidationException("Mật khẩu hiện tại không đúng");
        }

        taikhoan.setMatkhau(passwordEncoder.encode(request.getMatkhaumoi()));
        taikhoanRepository.save(taikhoan);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public boolean sendResetPasswordLink(String email) throws MessagingException, IOException {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Email không tồn tại, vui lòng nhập lại!"));
        String token = UUID.randomUUID().toString();
        taikhoan.setTokenxacthuc(token);
        taikhoan.setTokenhethan(new Timestamp(System.currentTimeMillis() + 15L * 60 * 1000));
        taikhoanRepository.save(taikhoan);
        emailService.sendResetPasswordLink(taikhoan, token);
        return true;
    }

    public boolean resetPassword(TaikhoanCreationRequest request, String token) {
        Taikhoan user = taikhoanRepository.findByTokenxacthuc(token)
                .orElseThrow(() -> new ValidationException("Token không hợp lệ"));
        if (user.getTokenhethan() != null &&
                user.getTokenhethan().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("Token đã hết hạn");
        }
        user.setMatkhau(passwordEncoder.encode(request.getMatkhau()));
        user.setTokenxacthuc(null);
        user.setTokenhethan(null);
        taikhoanRepository.save(user);
        return true;
    }

    public Taikhoan verifyKyc(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        taikhoan.setXacthuckyc(TrangThaiTaiKhoan.ACTIVE);
        taikhoanRepository.save(taikhoan);
        return taikhoan;
    }


    private List<String> internalAppend(List<MultipartFile> files) {
        Path dir = ensureImageDir();
        List<String> added = new ArrayList<>();
        int addedCount = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            if (addedCount >= 2) break;
            String original = f.getOriginalFilename();
            if (original == null || original.isBlank()) continue;
            String unique = resolveUniqueFilename(dir, sanitizeFilename(original));
            try {
                f.transferTo(dir.resolve(unique).toFile());
            } catch (IOException e) {
                throw new StorageException("Không thể lưu file " + original, e);
            }
            added.add(unique);
            addedCount++;
        }
        if (added.isEmpty()) {
            throw new ValidationException("Không có file hợp lệ để thêm");
        }
        return added;
    }

    private Path ensureImageDir() {
        String imgDir = System.getProperty("user.dir") + "/imgs/kyc";
        Path dirPath = Paths.get(imgDir);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            throw new StorageException("Không thể tạo thư mục lưu ảnh: " + imgDir, e);
        }
        return dirPath;
    }

    private String sanitizeFilename(String filename) {
        String name = filename;
        String ext = "";
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = name.substring(dotIdx).toLowerCase();
            name = name.substring(0, dotIdx);
        }
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-zA-Z0-9\\-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase();
        if (base.isBlank()) base = "image";
        return base + ext;
    }

    private String resolveUniqueFilename(Path dir, String filename) {
        Path p = dir.resolve(filename);
        if (!Files.exists(p)) return filename;

        String name = filename;
        String ext = "";
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = name.substring(dotIdx);
            name = name.substring(0, dotIdx);
        }
        int i = 1;
        while (true) {
            String next = name + "-" + i + ext;
            if (!Files.exists(dir.resolve(next))) return next;
            i++;
        }
    }
}
