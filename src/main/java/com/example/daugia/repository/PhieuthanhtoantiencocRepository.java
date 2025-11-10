package com.example.daugia.repository;

import com.example.daugia.entity.Phieuthanhtoantiencoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuthanhtoantiencocRepository extends JpaRepository<Phieuthanhtoantiencoc, String> {
    List<Phieuthanhtoantiencoc> findByTaiKhoan_Matk(String matk);
    Optional<Phieuthanhtoantiencoc> findByTaiKhoan_MatkAndPhienDauGia_Maphiendg(String matk, String maphiendg);
}
