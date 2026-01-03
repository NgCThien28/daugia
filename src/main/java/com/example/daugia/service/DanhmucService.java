package com.example.daugia.service;

import com.example.daugia.entity.Danhmuc;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.DanhmucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanhmucService {
    @Autowired
    private DanhmucRepository danhmucRepository;

    public List<Danhmuc> findAll() {
        return danhmucRepository.findAll();
    }

    public Danhmuc create (String tendm) {
        String normalizedName = tendm.trim();

        if (tendm.trim().isEmpty())
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        if (danhmucRepository.existsByTendmIgnoreCase(normalizedName))
            throw new ValidationException("Tên danh mục đã tồn tại");

        Danhmuc dm = new Danhmuc();
        dm.setTendm(normalizedName);

        return danhmucRepository.save(dm);
    }

    public Danhmuc update (String madm, String tendm) {
        Danhmuc dm = danhmucRepository.findById(madm)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));
        String normalizedName = tendm.trim();

        if (normalizedName.equalsIgnoreCase(dm.getTendm())) {
            return dm;
        }
        if (tendm.trim().isEmpty())
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        if (danhmucRepository.existsByTendmIgnoreCase(normalizedName))
            throw new ValidationException("Tên danh mục đã tồn tại");

        dm.setTendm(normalizedName);

        return danhmucRepository.save(dm);
    }

    public void delete (String madm) {
        Danhmuc dm = danhmucRepository.findById(madm)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        if (dm.getSanPham() != null && !dm.getSanPham().isEmpty()) {
            throw new ValidationException("Không thể xóa danh mục vì còn sản phẩm liên quan");
        }

        danhmucRepository.delete(dm);
    }
}
