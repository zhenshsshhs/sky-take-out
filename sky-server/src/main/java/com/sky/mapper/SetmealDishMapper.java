package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {



    List<Long> getSetmealIdsByDishIds(List<Long> ids);


    void insertBatch(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal where id=#{id}")
    Setmeal getSetmealByIds(Long id);

    void deleteBatch(List<Long> setMealIds);
}
