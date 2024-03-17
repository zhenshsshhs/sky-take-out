package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("定时清理超时订单：{}", LocalDateTime.now());
        // select * from orders where pay_status == 0 and 2024-03-17 20:13:30 < now+15mi
        Integer status = Orders.PENDING_PAYMENT;
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList=orderMapper.getBystatusAndOrderTimeLT(status,orderTime);
        if (ordersList!=null && ordersList.size()>0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliverOrder(){
        log.info("定时处理处于派送中的订单:{}",LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getDeliverStatus(Orders.DELIVERY_IN_PROGRESS);
        if (ordersList!=null && ordersList.size()>0) {
            for (Orders orders : ordersList) {
                orders.setDeliveryStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(orders);

            }
        }
    }
}
