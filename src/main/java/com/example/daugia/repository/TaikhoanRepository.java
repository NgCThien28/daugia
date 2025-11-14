package com.example.daugia.repository;

import com.example.daugia.entity.Taikhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaikhoanRepository extends JpaRepository<Taikhoan, String> {
    boolean existsByEmail(String email);
    Optional<Taikhoan> findByEmail(String email);
    Optional<Taikhoan> findByTokenxacthuc(String token);
}
