package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PhiendaugiaRepository extends JpaRepository<Phiendaugia, String> {
    List<Phiendaugia> findByTaiKhoan_Matk(String makh);

    boolean existsBySanPham_Masp(String masp);

    List<Phiendaugia> findByTrangthai(TrangThaiPhienDauGia trangthai);

    List<Phiendaugia> findByTrangthaiAndThoigianktBefore(TrangThaiPhienDauGia trangthai, Timestamp currentTime);

    Page<Phiendaugia> findByTrangthai(TrangThaiPhienDauGia trangthai, Pageable pageable);

    Page<Phiendaugia> findByTrangthaiIn(List<TrangThaiPhienDauGia> statuses, Pageable pageable);

    @Query("SELECT p FROM Phiendaugia p WHERE p.trangthai = :status " +
            "AND (:startDateFrom IS NULL OR p.thoigianbd >= :startDateFrom) " +
            "AND (:startDateTo IS NULL OR p.thoigianbd <= :startDateTo)")
    Page<Phiendaugia> findByTrangthaiAndThoigianbdBetween(@Param("status") TrangThaiPhienDauGia status,
                                                          @Param("startDateFrom") Long startDateFrom,
                                                          @Param("startDateTo") Long startDateTo,
                                                          Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Phiendaugia p
        JOIN p.sanPham sp
        LEFT JOIN sp.danhMuc dm
        LEFT JOIN sp.thanhPho tp
        WHERE (:statuses IS NULL OR p.trangthai IN :statuses)
        AND (:keyword IS NULL OR LOWER(sp.tensp) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:cateId IS NULL OR dm.madm = :cateId)
        AND (:regionId IS NULL OR tp.matp = :regionId)
        AND (:minPrice IS NULL OR p.giakhoidiem >= :minPrice)
        AND (:maxPrice IS NULL OR p.giakhoidiem <= :maxPrice)
        AND (:startDateFrom IS NULL OR p.thoigianbd >= :startDateFrom)
        AND (:startDateTo IS NULL OR p.thoigianbd <= :startDateTo)
        """)
    Page<Phiendaugia> findFilteredAuctions(
            @Param("statuses") List<TrangThaiPhienDauGia> statuses,
            @Param("keyword") String keyword,
            @Param("cateId") String cateId,
            @Param("regionId") String regionId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDateFrom") Long startDateFrom,
            @Param("startDateTo") Long startDateTo,
            Pageable pageable
    );

    @Query("""
            select distinct ph
            from Phiendaugia ph
            join ph.phieuThanhToanTienCoc tc
            where tc.taiKhoan.email = :email
              and tc.trangthai = com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc.PAID
            """)
    List<Phiendaugia> findAuctionsPaidByEmail(@Param("email") String email);

    // Page version with proper countQuery
    @Query(value = """
            select distinct ph
            from Phiendaugia ph
            join ph.phieuThanhToanTienCoc tc
            where tc.taiKhoan.email = :email
              and tc.trangthai = com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc.PAID
            """,
            countQuery = """
                    select count(distinct ph.maphiendg)
                    from Phiendaugia ph
                    join ph.phieuThanhToanTienCoc tc
                    where tc.taiKhoan.email = :email
                      and tc.trangthai = com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc.PAID
                    """)
    Page<Phiendaugia> findAuctionsPaidByEmail(@Param("email") String email, Pageable pageable);
}
