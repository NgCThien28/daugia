package com.example.daugia.repository;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.entity.Sanpham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, String> {
    Page<Sanpham> findAll(Specification<Sanpham> spec, Pageable pageable);
    Page<Sanpham> findByTrangthai(TrangThaiSanPham trangthai, Pageable pageable);
}
