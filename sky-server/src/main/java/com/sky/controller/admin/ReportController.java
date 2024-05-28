package com.sky.controller.admin;

import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.lettuce.core.output.ListOfGenericMapsOutput;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            //设置日期格式
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额数据统计:{},{}",begin,end);
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            //设置日期格式
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数据统计:{},{}",begin,end);
        return Result.success(reportService.getUserStatistics(begin,end));
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(
            //设置日期格式
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计:{},{}",begin,end);
        return Result.success(reportService.getOrderStatistics(begin,end));
    }



}
