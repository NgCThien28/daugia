package com.example.daugia.repository;

import com.example.daugia.entity.Taikhoanquantri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaikhoanquantriRepository extends JpaRepository<Taikhoanquantri, String> {
}
