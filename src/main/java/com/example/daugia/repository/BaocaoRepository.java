package com.example.daugia.repository;

import com.example.daugia.entity.Baocao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaocaoRepository extends JpaRepository<Baocao, String> {
}
