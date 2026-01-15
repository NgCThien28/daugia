package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
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
public interface PhieuthanhtoantiencocRepository extends JpaRepository<Phieuthanhtoantiencoc, String> {
    List<Phieuthanhtoantiencoc> findByTaiKhoan_Matk(String matk);

    Optional<Phieuthanhtoantiencoc> findByTaiKhoan_MatkAndPhienDauGia_Maphiendg(String matk, String maphiendg);

    Page<Phieuthanhtoantiencoc> findByTaiKhoan_MatkAndTrangthai(
            String matk,
            TrangThaiPhieuThanhToanTienCoc trangthai,
            Pageable pageable
    );

    List<Phieuthanhtoantiencoc> findByPhienDauGia_MaphiendgAndTrangthai(
            String maphiendg,
            TrangThaiPhieuThanhToanTienCoc trangthai
    );

    List<Phieuthanhtoantiencoc> findByPhienDauGia_Maphiendg(String maphiendg);

    Page<Phieuthanhtoantiencoc> findAll(Specification<Phieuthanhtoantiencoc> spec, Pageable pageable);

    @Query("""
            SELECT p
            FROM Phieuthanhtoantiencoc p
            WHERE(:from IS NULL OR p.thoigianthanhtoan >= :from)
            AND (:to IS NULL OR p.thoigianthanhtoan <= :to)
            AND (:status IS NULL OR p.trangthai = :status)
            """)
    List<Phieuthanhtoantiencoc> filter(
            @Param("from") Timestamp from,
            @Param("to") Timestamp to,
            @Param("status") TrangThaiPhieuThanhToanTienCoc status
    );
}
