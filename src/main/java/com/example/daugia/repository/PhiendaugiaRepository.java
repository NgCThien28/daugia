package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
