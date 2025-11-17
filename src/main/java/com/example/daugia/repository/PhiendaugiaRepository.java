package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
