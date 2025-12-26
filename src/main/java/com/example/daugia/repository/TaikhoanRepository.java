package com.example.daugia.repository;

import com.example.daugia.entity.Taikhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaikhoanRepository extends JpaRepository<Taikhoan, String> {
    Page<Taikhoan> findAll(Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Taikhoan> findByEmail(String email);

    Optional<Taikhoan> findByTokenxacthuc(String token);
}
