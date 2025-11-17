package com.example.daugia.service;

import com.example.daugia.entity.Thanhpho;
import com.example.daugia.repository.ThanhphoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThanhphoService {
    @Autowired
    private ThanhphoRepository thanhphoRepository;

    public List<Thanhpho> findAll() {
        return thanhphoRepository.findAll();
    }
}
