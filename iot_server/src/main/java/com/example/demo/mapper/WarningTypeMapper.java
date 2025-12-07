package com.example.demo.mapper;

import com.example.demo.entity.WarningType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WarningTypeMapper {

    /**
     * 查询所有报警类型
     */
    List<WarningType> findAll();

    /**
     * 根据ID查询
     */
    WarningType findById(Long id);
}

