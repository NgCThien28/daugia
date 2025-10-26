package com.example.daugia.repository;

import com.example.daugia.entity.Phieuthanhtoantiencoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuthanhtoantiencocRepository extends JpaRepository<Phieuthanhtoantiencoc, String> {
}
