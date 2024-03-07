package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，填充公共字段
 */
@Component
@Slf4j
@Aspect
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }
    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFull(JoinPoint joinPoint) throws Exception {
        log.info("开始进行公共字段自动填充。。。");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);

        //获得到当前被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object entity = joinPoint.getArgs()[0];

        // 准备赋值数据
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        //根据不同的操作类型，为对应的属性赋值通过反射来赋值

        if(annotation.value()== OperationType.INSERT){
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setCreateUser.invoke(entity,currentId);
            setCreateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);

        }else if(annotation.value()==OperationType.UPDATE){
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

            setUpdateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);


        }
    }
}
