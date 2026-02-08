package com.example.moneytransfer.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Around("execution(* com.example.moneytransfer.service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();

        log.info("Service method invoked: {} with arguments: {}", methodName, Arrays.toString(args));

        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Service method completed: {} in {} ms", methodName, duration);
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;
            log.error("Service method failed: {} after {} ms with: {}", methodName, duration, t.getMessage());
            throw t;
        }
    }
}
