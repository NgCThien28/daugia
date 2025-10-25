package com.example.daugia.repository;

import com.example.daugia.entity.Taikhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaikhoanRepository extends JpaRepository<Taikhoan, String> {

}
