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
        // Tìm sản phẩm
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        // Kiểm tra files
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Không có file nào được gửi lên");
        }

        // Tạo thư mục nếu chưa có
        String imgDir = System.getProperty("user.dir") + "/imgs";
        Path dirPath = Paths.get(imgDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Lấy danh sách hình ảnh hiện tại của sản phẩm
        List<Hinhanh> currentImages = sanpham.getHinhAnh();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
            sanpham.setHinhAnh(currentImages);
        }

        List<Hinhanh> updatedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);

            // Lưu file vào thư mục (đè lên file cũ nếu trùng tên)
            file.transferTo(filePath.toFile());

            // Kiểm tra xem đã có hình ảnh với tên này trong DB chưa
            Hinhanh existingImage = currentImages.stream()
                    .filter(h -> h.getTenanh().equals(filename))
                    .findFirst()
                    .orElse(null);

            if (existingImage != null) {
                // Nếu đã tồn tại, giữ nguyên record trong DB (file đã được đè)
                updatedImages.add(existingImage);
            } else {
                // Nếu chưa tồn tại, tạo record mới
                Hinhanh h = new Hinhanh();
                h.setTenanh(filename);
                h.setSanPham(sanpham);
                Hinhanh savedImage = hinhanhRepository.save(h);

                // Thêm vào list hiện tại của sản phẩm
                currentImages.add(savedImage);
                updatedImages.add(savedImage);
            }
        }

        return updatedImages;
    }

}
