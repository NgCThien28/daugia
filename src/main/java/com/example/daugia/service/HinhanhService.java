package com.example.daugia.service;

import com.example.daugia.entity.Hinhanh;
import com.example.daugia.repository.HinhanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HinhanhService {
    @Autowired
    private HinhanhRepository hinhanhRepository;

    public List<Hinhanh> findAll(){
        return hinhanhRepository.findAll();
    }
}
