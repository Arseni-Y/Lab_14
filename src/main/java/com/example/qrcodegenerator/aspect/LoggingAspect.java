package com.example.qrcodegenerator.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect
{
    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods()
    {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods()
    {
    }

    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable
    {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("Entering method: {}", methodName);

        try
        {
            Object result = joinPoint.proceed();
            log.info("Exiting method: {}", methodName);
            return result;
        }
        catch (Throwable t)
        {
            log.error("Exception in method: {} - {}", methodName, t.getMessage(), t);
            throw t;
        }
    }
}