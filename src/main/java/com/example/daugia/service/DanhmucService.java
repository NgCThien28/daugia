package com.example.daugia.service;

import com.example.daugia.entity.Danhmuc;
import com.example.daugia.repository.DanhmucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanhmucService {
    @Autowired
    private DanhmucRepository danhmucRepository;

    public List<Danhmuc> findAll() {
        return danhmucRepository.findAll();
    }
}
