package com.example.daugia.repository;

import com.example.daugia.entity.Phientragia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhientragiaRepository extends JpaRepository<Phientragia, String> {
    Optional<Phientragia> findTopByTaiKhoan_MatkAndPhienDauGia_MaphiendgOrderByThoigianDesc(String makh, String maphiendg);

    Optional<Phientragia> findTopByPhienDauGia_MaphiendgOrderBySotienDesc(String maphiendg);

    // Thêm methods mới cho scheduler
    List<Phientragia> findByPhienDauGia_Maphiendg(String maphiendg);

    @Query("SELECT MAX(p.sotien) FROM Phientragia p WHERE p.phienDauGia.maphiendg = :maphiendg")
    Optional<BigDecimal> findMaxSotienByPhienDauGia_Maphiendg(@Param("maphiendg") String maphiendg);

}