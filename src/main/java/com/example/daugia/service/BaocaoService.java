package com.example.daugia.service;

import com.example.daugia.entity.Baocao;
import com.example.daugia.repository.BaocaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaocaoService {
    @Autowired
    private BaocaoRepository baocaoRepository;

    public List<Baocao> findAll(){
        return baocaoRepository.findAll();
    }
}
