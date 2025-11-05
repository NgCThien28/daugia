package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhiendaugiaRepository extends JpaRepository<Phiendaugia, String> {
    List<Phiendaugia> findByTaiKhoan_Matk(String makh);
    List<Phiendaugia> findByTrangthai(TrangThaiPhienDauGia trangthai);
}
