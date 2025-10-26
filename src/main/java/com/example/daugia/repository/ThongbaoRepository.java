package com.example.daugia.repository;

import com.example.daugia.entity.Thongbao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThongbaoRepository extends JpaRepository<Thongbao, String> {

}
