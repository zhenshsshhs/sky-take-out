package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;

@Mapper
public interface ShoppingCartMapper {



    void add(ShoppingCart shoppingCart);

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Select("select * from shopping_cart where user_id=#{id}")
    List<ShoppingCart> getByUserId(Long id);
    @Update("update shopping_cart  set number=#{number} where id = #{id}")
    void updateNumberById(ShoppingCart cart);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id=#{id}")
    void deleteByUserId(Long id);

}
