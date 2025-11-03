package com.example.daugia.repository;

import com.example.daugia.entity.Sanpham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, String> {
    List<Sanpham> findByTaiKhoan_Matk(String makh);
}
