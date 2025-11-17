package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    long countByPhienDauGia_MaphiendgAndTrangthai(
            String maphiendg,
            TrangThaiPhieuThanhToanTienCoc trangthai
    );

    List<Phieuthanhtoantiencoc> findByPhienDauGia_MaphiendgAndTrangthai(
            String maphiendg,
            TrangThaiPhieuThanhToanTienCoc trangthai
    );
}
