package com.example.daugia.repository;

import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuthanhtoanRepository extends JpaRepository<Phieuthanhtoan, String> {
    Optional<Phieuthanhtoan> findByPhienDauGia_Maphiendg(String maphiendg);
    Page<Phieuthanhtoan> findByTaiKhoan_MatkAndTrangthai(String matk, TrangThaiPhieuThanhToan status, Pageable pageable);

    List<Phieuthanhtoan> findByTaiKhoan_Matk(String matk);
}