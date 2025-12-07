package com.example.demo.mapper;

import com.example.demo.entity.BreedType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BreedTypeMapper {

    /**
     * 查询所有养殖类型
     */
    List<BreedType> findAll();

    /**
     * 根据ID查询
     */
    BreedType findById(Long id);
}

