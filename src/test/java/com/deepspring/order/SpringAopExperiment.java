package com.deepspring.order;

import com.deepspring.member.MemoryMemberRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

public class SpringAopExperiment {

    @Aspect
    @Component
    static class TimeLoggingAspect {
        @Around("execution(* com.deepspring.order.OrderServiceInterface.*(..))")
        public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed(); // 손수 만든 InvocationHandler.invoke() / MethodInterceptor.intercept()랑 같은 역할
            long end = System.currentTimeMillis();
            System.out.println(joinPoint.getSignature().getName() + " 실행시간 = " + (end - start) + "ms");
            return result;
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class AopConfig {
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(
                    AopConfig.class,
                    TimeLoggingAspect.class,
                    MemoryMemberRepository.class,
                    FixDiscountPolicy.class,
                    OrderService.class
            );
            context.refresh();

            OrderServiceInterface bean = context.getBean(OrderServiceInterface.class);
            System.out.println("bean.getClass() = " + bean.getClass());
            System.out.println("isAopProxy? " + AopUtils.isAopProxy(bean));
            System.out.println("isJdkDynamicProxy? " + AopUtils.isJdkDynamicProxy(bean));
            System.out.println("isCglibProxy? " + AopUtils.isCglibProxy(bean));

            System.out.println();
            System.out.println("=== 외부에서 직접 calculatePrice 호출 (프록시 거침) ===");
            int result = bean.calculatePrice(1L, 10000);
            System.out.println("result = " + result);

            System.out.println();
            System.out.println("=== outerCall 호출 (내부에서 this.calculatePrice() 호출, Self Invocation) ===");
            int outerResult = bean.outerCall(1L, 10000);
            System.out.println("outerResult = " + outerResult);
        }
    }
}
