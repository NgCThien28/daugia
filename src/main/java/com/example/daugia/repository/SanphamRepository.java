package com.example.daugia.repository;

import com.example.daugia.entity.Sanpham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, String> {
//    Page<Sanpham> findByTaiKhoan_MatkAndTrangthaiIn(String makh,
//                                                    List<TrangThaiSanPham> trangthai,
//                                                    Pageable pageable);

    Page<Sanpham> findAll(Specification<Sanpham> spec, Pageable pageable);
}
