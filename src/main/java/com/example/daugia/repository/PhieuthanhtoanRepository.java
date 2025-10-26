package com.example.daugia.repository;

import com.example.daugia.entity.Phieuthanhtoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuthanhtoanRepository extends JpaRepository<Phieuthanhtoan, String> {
}
