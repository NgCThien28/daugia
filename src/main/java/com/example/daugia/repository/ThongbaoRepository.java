package com.example.daugia.repository;

import com.example.daugia.entity.Thongbao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongbaoRepository extends JpaRepository<Thongbao, String> {
    List<Thongbao> findByTaiKhoan_Matk(String matk);
    Page<Thongbao> findByTaiKhoan_Matk(String matk, Pageable pageable);
}
