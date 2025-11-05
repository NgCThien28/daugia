package com.example.daugia.service;

import com.example.daugia.entity.Hinhanh;
import com.example.daugia.entity.Sanpham;
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
    public List<Hinhanh> saveFiles(String masp, List<MultipartFile> files) throws IOException {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Không có file nào được gửi lên");
        }

        String imgDir = System.getProperty("user.dir") + "/imgs";
        Path dirPath = Paths.get(imgDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        List<Hinhanh> savedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            file.transferTo(filePath.toFile());

            Hinhanh h = new Hinhanh();
            h.setTenanh(filename);
            h.setSanPham(sanpham);
            savedImages.add(hinhanhRepository.save(h));
        }

        return savedImages;
    }

    @Transactional
    public List<Hinhanh> updateFiles(String masp, List<MultipartFile> files) throws IOException {
        // 1️⃣ Tìm sản phẩm
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Không có file nào được gửi lên");
        }

        // 2️⃣ Đường dẫn thư mục ảnh
        String imgDir = System.getProperty("user.dir") + "/imgs";
        Path dirPath = Paths.get(imgDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 3️⃣ Lấy danh sách ảnh hiện tại
        List<Hinhanh> currentImages = sanpham.getHinhAnh();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
            sanpham.setHinhAnh(currentImages);
        }

        // 4️⃣ Duyệt qua từng file được gửi từ FE
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();

            // Làm sạch tên file (bỏ dấu, lowercase, thay khoảng trắng bằng '-')
            String cleanName = Normalizer.normalize(originalName, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replaceAll("[^a-zA-Z0-9\\.\\-]", "-")
                    .toLowerCase();

            Path targetPath = dirPath.resolve(cleanName);

            // Nếu đã có ảnh ở vị trí này thì xóa file cũ và update record
            if (i < currentImages.size()) {
                Hinhanh oldImage = currentImages.get(i);

                // Xóa file cũ trong thư mục nếu tồn tại
                Path oldPath = dirPath.resolve(oldImage.getTenanh());
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    System.err.println("Không thể xóa file cũ: " + oldPath);
                }

                // Lưu file mới
                file.transferTo(targetPath.toFile());

                // Cập nhật record DB
                oldImage.setTenanh(cleanName);
                hinhanhRepository.save(oldImage);
            } else {
                // Nếu chưa có ảnh ở vị trí này, thêm mới
                file.transferTo(targetPath.toFile());

                Hinhanh newImg = new Hinhanh();
                newImg.setTenanh(cleanName);
                newImg.setSanPham(sanpham);
                hinhanhRepository.save(newImg);

                currentImages.add(newImg);
            }
        }

        // 5️⃣ Giữ tối đa 3 ảnh
        if (currentImages.size() > 3) {
            // Xóa file dư + record dư
            for (int i = 3; i < currentImages.size(); i++) {
                Hinhanh extra = currentImages.get(i);
                Path extraPath = dirPath.resolve(extra.getTenanh());
                Files.deleteIfExists(extraPath);
                hinhanhRepository.delete(extra);
            }
            currentImages = currentImages.subList(0, 3);
        }

        // 6️⃣ Lưu cập nhật vào DB
        sanpham.setHinhAnh(currentImages);
        sanphamRepository.save(sanpham);

        return currentImages;
    }

}
