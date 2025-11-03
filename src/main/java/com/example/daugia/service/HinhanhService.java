package com.example.daugia.service;

import com.example.daugia.dto.request.HinhanhCreationRequest;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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

}
