package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    //查询的是订单表
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;
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

            // select sum(amount) from orders where order_time > begin and order_time < end and status = 5;

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

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {


        List<LocalDate> dateList = getDateList(begin, end);

        //每日订单列表
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单类别
        List<Integer> validOrderCountList = new ArrayList<>();
        //订单总数
        Integer totalOrderCount = 0;
        //有效订单数
        Integer validOrderCount = 0;

        for (LocalDate date : dateList) {
            //查询data日期对应的营业额数据 营业额:状态为"已完成"的订单金额合计

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//2024-05-25 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//2024-05-25 23:59:59

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer order = orderMapper.countByMap(map);
            map.put("status",Orders.COMPLETED);
            Integer validOrder = orderMapper.countByMap(map);

            orderCountList.add(order);
            validOrderCountList.add(validOrder);
            totalOrderCount +=order;
            validOrderCount +=validOrder;
        }

        //订单完成率
        Double orderCompletionRate =0.0;
        if(totalOrderCount != 0) {
            //计算订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /**
     * 根据指定的开始日期 结束日期 返回两个日期之间的日期列表数据
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        //用于存在从begin到end范围内每天日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)){
            //日期计算 指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }


    /**
     * 销量top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//2024-05-25 00:00:00
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);//2024-05-25 23:59:59


        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        //List拆分为 name List 和number List
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }


    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response){
        //1.查询数据库 获取营业数据--查询最近30天的

        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVo = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));


        //2.通过POI将数据写入到Excel文件中

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");


        try {
            //基于模板文件创建新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //2.1填充概览数据
            
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间:"+dateBegin+"至"+dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVo.getTurnover());
            row.getCell(4).setCellValue(businessDataVo.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVo.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVo.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVo.getUnitPrice());

            //2.2填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getValidOrderCount());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3.通过输出流讲Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }
}
