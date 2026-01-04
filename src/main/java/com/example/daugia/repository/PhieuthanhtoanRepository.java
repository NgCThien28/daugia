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

    @Query("""
            SELECT DATE(p.thoigianthanhtoan) as date, COUNT(p) as count
            FROM Phieuthanhtoan p
            WHERE p.trangthai = :status
            AND (:from IS NULL OR p.thoigianthanhtoan >= :from)
            AND (:to IS NULL OR p.thoigianthanhtoan <= :to)
            GROUP BY DATE(p.thoigianthanhtoan)
            ORDER BY DATE(p.thoigianthanhtoan)
            """)
    List<Object[]> findSuccessfulTransactions(
            @Param("from") Timestamp from,
            @Param("to") Timestamp to,
            @Param("status") TrangThaiPhieuThanhToan status
    );

    @Query("""
            SELECT DATE(p.thoigianthanhtoan) as date, SUM(p.sotien) as total
            FROM Phieuthanhtoan p
            WHERE p.trangthai = :status
            AND (:from IS NULL OR p.thoigianthanhtoan >= :from)
            AND (:to IS NULL OR p.thoigianthanhtoan <= :to)
            GROUP BY DATE(p.thoigianthanhtoan)
            ORDER BY DATE(p.thoigianthanhtoan)
            """)
    List<Object[]> findGrossMerchandiseValue(
            @Param("from") Timestamp from,
            @Param("to") Timestamp to,
            @Param("status") TrangThaiPhieuThanhToan status
    );

    @Query("""
            SELECT DATE(ptt.thoigianthanhtoan) as date, SUM(ptt.sotien * sp.hoahong) AS total
            FROM Phieuthanhtoan ptt
            JOIN ptt.phienDauGia dg
            JOIN dg.sanPham sp
            WHERE ptt.trangthai = :status
              AND (:from IS NULL OR ptt.thoigianthanhtoan >= :from)
              AND (:to IS NULL OR ptt.thoigianthanhtoan <= :to)
            GROUP BY DATE(ptt.thoigianthanhtoan)
            ORDER BY DATE(ptt.thoigianthanhtoan)
            """)
    List<Object[]> findCommission(
            @Param("from") Timestamp from,
            @Param("to") Timestamp to,
            @Param("status") TrangThaiPhieuThanhToan status
    );
}