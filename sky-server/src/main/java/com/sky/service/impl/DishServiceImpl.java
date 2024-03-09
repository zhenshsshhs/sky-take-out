package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired(required = true)
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 保存菜品和风味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 赋值
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();

        // 插入一条菜品信息
        dishMapper.save(dish);
        Long dishId = dish.getId();

        // 插入n条口味信息
        if (flavors!=null && flavors.size()>0) {
            flavors.forEach(flavor->{
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> dish=dishMapper.page(dishPageQueryDTO);
        return new PageResult(dish.getTotal(), dish);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //是否存在起售中的
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 是否存在套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);


        if (setmealIds!=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }


        // 删除菜品数据
        dishMapper.delete(ids);

        // 删除关联的口味
        dishFlavorMapper.deleteByDishId(ids);

    }



    @Override
    public DishVO getByidWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        List<DishFlavor> dishFlavor=dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(dishFlavor);

        return dishVO;
    }
    @Transactional
    @Override
    public void updateDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 删除口味
        Long id = dishDTO.getId();
        List<Long> ids = Arrays.asList(new Long[]{id});
//        id.toCharArray
        dishFlavorMapper.deleteByDishId(ids);


        if (flavors.size()>0 && flavors!=null){
            flavors.forEach(flavor->{
                flavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);

        }
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


}
