package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.entity.Phieuthanhtoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuthanhtoanRepository extends JpaRepository<Phieuthanhtoan, String> {
    Optional<Phieuthanhtoan> findByPhienDauGia_Maphiendg(String maphiendg);
//    Page<Phieuthanhtoan> findByTaiKhoan_MatkAndTrangthai(String matk, TrangThaiPhieuThanhToan status, Pageable pageable);

    List<Phieuthanhtoan> findByTaiKhoan_Matk(String matk);

    Page<Phieuthanhtoan> findAll(Specification<Phieuthanhtoan> spec, Pageable pageable);

    @Query("""
            SELECT p
            FROM Phieuthanhtoan p
            WHERE(:from IS NULL OR p.thoigianthanhtoan >= :from)
            AND (:to IS NULL OR p.thoigianthanhtoan <= :to)
            AND (:status IS NULL OR p.trangthai = :status)
            """)
    List<Phieuthanhtoan> filter(
            @Param("from") Timestamp from,
            @Param("to") Timestamp to,
            @Param("status") TrangThaiPhieuThanhToan status
    );
}