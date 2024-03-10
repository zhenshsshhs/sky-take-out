package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        setmealDTO.setId(setmeal.getId());

        // 新增套餐菜品
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        if (setmealDishes!=null){
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmeals=setmealMapper.page(setmealPageQueryDTO);
        PageResult pageResult = new PageResult(setmeals.getTotal(), setmeals);

        return pageResult;


    }

    @Override
    public SetmealVO getSetmealById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal seaMeal = setmealDishMapper.getSetmealByIds(id);
        BeanUtils.copyProperties(seaMeal,setmealVO);
        if(seaMeal!=null){
            List<SetmealDish> setmealDishes = setmealMapper.getSetmeaDishBySetmealId(id);
            setmealVO.setSetmealDishes(setmealDishes);
        }
        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 更新套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        // 删除套餐菜品
        Long setmealId = setmeal.getId();
        List<Long> setmealIds = new ArrayList<>();
        setmealIds.add(setmealId);
        setmealDishMapper.deleteBatch(setmealIds);

        // 新增套餐菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        if (setmealDishes!=null){
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Transactional
    @Override
    public void delete(List<Long> setMealIds) {
        setmealMapper.deleteBySetMealIds(setMealIds);
        System.out.println();
        setmealDishMapper.deleteBatch(setMealIds);
        System.out.println();
    }

    @Override
    public void setStatus(Long id, Integer status) {
        Setmeal setmeal = Setmeal.builder()
                        .id(id)
                                .status(status)
                                        .build();

        setmealMapper.update(setmeal);
    }
}
