package com.example.daugia.repository;

import com.example.daugia.entity.Thanhpho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThanhphoRepository extends JpaRepository<Thanhpho, String> {
}
