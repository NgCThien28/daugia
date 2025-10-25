package com.example.daugia.service;

import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.repository.TaikhoanquantriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaikhoanquantriService {
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;

    public List<Taikhoanquantri> findAll() {
        return taikhoanquantriRepository.findAll();
    }
}
