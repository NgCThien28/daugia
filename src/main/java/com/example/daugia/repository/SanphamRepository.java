package com.example.daugia.repository;

import com.example.daugia.entity.Sanpham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanphamRepository extends JpaRepository<Sanpham, String> {
}
