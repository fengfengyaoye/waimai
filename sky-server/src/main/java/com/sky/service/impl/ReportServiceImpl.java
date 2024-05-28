package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class ReportServiceImpl implements ReportService {

    //查询的是订单表
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //用于存在从begin到end范围内每天日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算 指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询data日期对应的营业额数据 营业额:状态为"已完成"的订单金额合计

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//2024-05-25 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//2024-05-25 23:59:59

            //# select sum(amount) from orders where order_time > begin and order_time < end and status = 5;
            //select sum(amount) from orders where order_time like '2024-05-25%';
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            //需要三个参数
            Double turnover = orderMapper.sumByMap(map);
            //如果当天没有营业额数据 turnover为null 将其转换为0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))//将list集合按照","进行分割 并转换为字符串
                .turnoverList(StringUtils.join(turnoverList, ","))//将list集合按照","进行分割 并转换为字符串
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //用于存在从begin到end范围内每天日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算 指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天新增用户
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        //需要查user表 统计用户
        //每天新增
        //# select sum(amount) from user where create_time > begin and crate_time < end;
        //每天新增
        //# select sum(amount) from user where crate_time < end;

        for (LocalDate date : dateList) {
            //查询data日期对应的营业额数据 营业额:状态为"已完成"的订单金额合计

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//2024-05-25 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//2024-05-25 23:59:59

            Map map = new HashMap();

            map.put("end",endTime);
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin",beginTime);
            Integer newUser = userMapper.countByMap(map);
            //这里如果没有数据不用转换 因为count查询的就是0
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
}
