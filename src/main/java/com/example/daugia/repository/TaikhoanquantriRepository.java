package com.example.daugia.repository;

import com.example.daugia.entity.Taikhoanquantri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaikhoanquantriRepository extends JpaRepository<Taikhoanquantri, String> {
    Optional<Taikhoanquantri> findByEmail(String email);
}
