package com.example.daugia.service;

import com.example.daugia.entity.Hinhanh;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.StorageException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.HinhanhRepository;
import com.example.daugia.repository.SanphamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class HinhanhService {
    @Autowired
    private HinhanhRepository hinhanhRepository;
    @Autowired
    private SanphamRepository sanphamRepository;

    public List<Hinhanh> findAll(){
        return hinhanhRepository.findAll();
    }

    @Transactional
    public List<Hinhanh> saveFiles(String masp, List<MultipartFile> files) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

        if (files == null || files.isEmpty()) {
            throw new ValidationException("Không có file nào được gửi lên");
        }

        Path dirPath = ensureImageDir();

        List<Hinhanh> savedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) continue;

            String cleanName = sanitizeFilename(original);
            String uniqueName = resolveUniqueFilename(dirPath, cleanName);

            try {
                Path filePath = dirPath.resolve(uniqueName);
                file.transferTo(filePath.toFile());
            } catch (IOException e) {
                throw new StorageException("Không thể lưu file: " + cleanName, e);
            }

            Hinhanh h = new Hinhanh();
            h.setTenanh(uniqueName);
            h.setSanPham(sanpham);
            savedImages.add(hinhanhRepository.save(h));
        }

        if (savedImages.isEmpty()) {
            throw new ValidationException("Không có file hợp lệ để lưu");
        }

        return savedImages;
    }

    @Transactional
    public List<Hinhanh> updateFiles(String masp, List<MultipartFile> files) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

        if (files == null || files.isEmpty()) {
            throw new ValidationException("Không có file nào được gửi lên");
        }

        Path dirPath = ensureImageDir();

        List<Hinhanh> currentImages = sanpham.getHinhAnh();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
            sanpham.setHinhAnh(currentImages);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) continue;

            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) continue;

            String cleanName = sanitizeFilename(original);
            String uniqueName = resolveUniqueFilename(dirPath, cleanName);
            Path targetPath = dirPath.resolve(uniqueName);

            try {
                if (i < currentImages.size()) {
                    // Cập nhật ảnh tại vị trí i
                    Hinhanh oldImage = currentImages.get(i);

                    // Xóa file cũ nếu có
                    Path oldPath = dirPath.resolve(oldImage.getTenanh());
                    try {
                        Files.deleteIfExists(oldPath);
                    } catch (IOException ignore) {
                        // Không chặn tiến trình nếu không xóa được file cũ
                    }

                    // Lưu file mới
                    file.transferTo(targetPath.toFile());

                    // Cập nhật DB
                    oldImage.setTenanh(uniqueName);
                    hinhanhRepository.save(oldImage);
                } else {
                    // Thêm ảnh mới
                    file.transferTo(targetPath.toFile());
                    Hinhanh newImg = new Hinhanh();
                    newImg.setTenanh(uniqueName);
                    newImg.setSanPham(sanpham);
                    hinhanhRepository.save(newImg);
                    currentImages.add(newImg);
                }
            } catch (IOException e) {
                throw new StorageException("Không thể lưu file: " + original, e);
            }
        }

        // Giữ tối đa 3 ảnh
        if (currentImages.size() > 3) {
            for (int i = 3; i < currentImages.size(); i++) {
                Hinhanh extra = currentImages.get(i);
                Path extraPath = dirPath.resolve(extra.getTenanh());
                try {
                    Files.deleteIfExists(extraPath);
                } catch (IOException ignore) {}
                hinhanhRepository.delete(extra);
            }
            currentImages = currentImages.subList(0, 3);
        }

        sanpham.setHinhAnh(currentImages);
        sanphamRepository.save(sanpham);

        return currentImages;
    }

    /* ================= Helpers ================= */

    private Path ensureImageDir() {
        String imgDir = System.getProperty("user.dir") + "/imgs";
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
        // Tách phần mở rộng
        String ext = "";
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0) {
            ext = name.substring(dotIdx).toLowerCase();
            name = name.substring(0, dotIdx);
        }
        // Bỏ dấu + ký tự đặc biệt
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
