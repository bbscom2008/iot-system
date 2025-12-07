package com.example.demo.service;

import com.example.demo.entity.WarningType;
import com.example.demo.mapper.WarningTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarningTypeService {

    private final WarningTypeMapper warningTypeMapper;

    /**
     * 查询所有报警类型
     */
    public List<WarningType> findAll() {
        return warningTypeMapper.findAll();
    }

    /**
     * 根据ID查询
     */
    public WarningType findById(Long id) {
        return warningTypeMapper.findById(id);
    }
}

