package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 自定义切面类 实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    //拦截com.sky.mapper下面的所有类的所有方法 返回值不限 参数不限
    //同时还要满足该方法上加入了@AutoFill
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}


    //这里用前置通知 要在执行insert update方法之前要用上通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        log.info("开始进行公共字段填充...");

        //获取到当前被拦截到的方法上的数据库操作类型

        //Signature接口提供的信息是比较一般的，例如可以获取方法名，但无法获取方法的参数类型、返回类型等更详细的信息
        //为了能够访问这些更详细的方法签名信息，需要将Signature对象转型为MethodSignature对象。MethodSignature是Signature的子接口
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获得方法前面对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法注解对象
        OperationType operationType = autoFill.value();//获得数据库操作对象

        //获取当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();//这个是获取所有参数
        if(args == null || args.length==0){//如果没有参数则直接结束
            return;
        }
        Object entity =  args[0];//约定第一个参数为实体对象

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型 为对应的属性通过反射来赋值
        if(operationType==OperationType.INSERT){
            //为4个公共字段赋值
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);//time
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);//id
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setCreateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);

        } else if (operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);

        }


    }


}
