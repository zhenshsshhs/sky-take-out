package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO getTurnoverStatics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatics(LocalDate begin, LocalDate end);
}
