package com.example.daugia.repository;

import com.example.daugia.entity.Hinhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HinhanhRepository extends JpaRepository<Hinhanh, String> {
}
