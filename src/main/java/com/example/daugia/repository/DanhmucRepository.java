package com.example.daugia.repository;

import com.example.daugia.entity.Danhmuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhmucRepository extends JpaRepository<Danhmuc, String> {
}
