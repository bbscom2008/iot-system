package com.example.demo.service;

import com.example.demo.entity.BreedType;
import com.example.demo.mapper.BreedTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BreedTypeService {

    private final BreedTypeMapper breedTypeMapper;

    /**
     * 查询所有养殖类型
     */
    public List<BreedType> findAll() {
        return breedTypeMapper.findAll();
    }

    /**
     * 根据ID查询
     */
    public BreedType findById(Long id) {
        return breedTypeMapper.findById(id);
    }
}

