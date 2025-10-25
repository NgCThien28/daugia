package com.example.daugia.service;

import com.example.daugia.entity.Taikhoan;
import com.example.daugia.repository.TaikhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaikhoanService {
    @Autowired
    private TaikhoanRepository taikhoanRepository;

    public List<Taikhoan> findAll(){
        return taikhoanRepository.findAll();
    }
}
