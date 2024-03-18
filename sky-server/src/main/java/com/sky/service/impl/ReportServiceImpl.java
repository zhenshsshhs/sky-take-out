package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO getTurnoverStatics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate cur=begin; cur.isBefore(end.plusDays(1));cur=cur.plusDays(1)){
            dateList.add(cur);
        }
        List<Double> turnoverList =new ArrayList<>();
        for (LocalDate data : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(data, LocalTime.MIN);
            LocalDateTime endTime  = LocalDateTime.of(data, LocalTime.MAX);
            Map map =new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover =orderMapper.getByMap(map);
            turnover = turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }


        TurnoverReportVO build = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();

        return build;
    }

    @Override
    public UserReportVO getUserStatics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate cur= begin; cur.isBefore(end.plusDays(1)) ;cur=cur.plusDays(1)) {
            dateList.add(cur);
        }
        List<Integer> newuserList = new ArrayList<>();
        List<Integer> totaluserList = new ArrayList<>();

        for (LocalDate data:dateList){
            LocalDateTime beginDate = LocalDateTime.of(data,LocalTime.MIN);
            LocalDateTime endDate = LocalDateTime.of(data,LocalTime.MAX);

            Map map = new HashMap();
            Integer totalUser=userMapper.getByMap(map);
            map.put("end",endDate);
            totalUser =totalUser==null?0:totalUser;

            map.put("begin",beginDate);

            Integer newUser=userMapper.getByMap(map);
            newUser =newUser==null?0:newUser;

            totaluserList.add(totalUser);
            newuserList.add(newUser);

        }

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newuserList,","))
                .totalUserList(StringUtils.join(totaluserList,","))
                .build();
        return userReportVO;
    }

    /**
     * 根据时间区间统计订单数量
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 每天订单总数集合
        List<Integer> orderCountList = new ArrayList<>();
        // 每天有效订单数集合
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 查询每天的总订单数 select count(id) from orders where order_time > ? and order_time <
            // ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time <
            // ? and status = ?
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        // 时间区间内的总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 时间区间内的总有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    /**
     * 查询指定时间区间内的销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        String nameList = StringUtils
                .join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils
                .join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 根据时间区间统计指定状态的订单数量
     *
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("begin", beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }
}
