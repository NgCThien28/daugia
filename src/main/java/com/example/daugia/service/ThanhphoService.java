package com.example.daugia.service;

import com.example.daugia.entity.Thanhpho;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.ThanhphoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThanhphoService {
    @Autowired
    private ThanhphoRepository thanhphoRepository;

    public List<Thanhpho> findAll() {
        return thanhphoRepository.findAll();
    }

    public Thanhpho create (String tentp) {
        String normalizedName = tentp.trim();

        if (tentp.trim().isEmpty())
            throw new IllegalArgumentException("Tên thành phố không được để trống");
        if (thanhphoRepository.existsByTentpIgnoreCase(normalizedName))
            throw new ValidationException("Tên thành phố đã tồn tại");

        Thanhpho tp = new Thanhpho();
        tp.setTentp(normalizedName);

        return thanhphoRepository.save(tp);
    }

    public Thanhpho update (String matp, String tentp) {
        Thanhpho tp = thanhphoRepository.findById(matp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố"));
        String normalizedName = tentp.trim();

        if (normalizedName.equalsIgnoreCase(tp.getTentp())) {
            return tp;
        }
        if (tentp.trim().isEmpty())
            throw new IllegalArgumentException("Tên thành phố không được để trống");
        if (thanhphoRepository.existsByTentpIgnoreCase(normalizedName))
            throw new ValidationException("Tên thành phố đã tồn tại");

        tp.setTentp(normalizedName);

        return thanhphoRepository.save(tp);
    }

    public void delete (String matp) {
        Thanhpho tp = thanhphoRepository.findById(matp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố"));

        if (tp.getSanphamList() != null && !tp.getSanphamList().isEmpty()) {
            throw new ValidationException("Không thể xóa thành phố vì còn sản phẩm liên quan");
        }

        if (tp.getTaikhoanList() != null && !tp.getTaikhoanList().isEmpty()) {
            throw new ValidationException("Không thể xóa thành phố vì còn tài khoản liên quan");
        }

        thanhphoRepository.delete(tp);
    }
}
