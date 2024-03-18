package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
}
