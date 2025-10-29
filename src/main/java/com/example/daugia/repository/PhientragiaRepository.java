package com.example.daugia.repository;

import com.example.daugia.entity.Phientragia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Repository
public interface PhientragiaRepository extends JpaRepository<Phientragia, String> {
    Optional<Phientragia> findTopByTaiKhoan_MatkAndPhienDauGia_MaphiendgOrderByThoigianDesc(String makh, String maphiendg);

}
