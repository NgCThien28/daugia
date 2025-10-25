package com.example.daugia.repository;

import com.example.daugia.entity.Phiendaugia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhiendaugiaRepository extends JpaRepository<Phiendaugia, String> {

}
