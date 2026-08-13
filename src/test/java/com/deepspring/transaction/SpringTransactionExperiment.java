package com.deepspring.transaction;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringTransactionExperiment {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TxConfig.class);

        SpringAccountTransferService service = context.getBean(SpringAccountTransferService.class);

        System.out.println("bean.getClass() = " + service.getClass());
        System.out.println("isAopProxy = " + AopUtils.isAopProxy(service));
        System.out.println("isCglibProxy = " + AopUtils.isCglibProxy(service));
        System.out.println("isJdkDynamicProxy = " + AopUtils.isJdkDynamicProxy(service));

        successCase(service);
        failureCase(service);

        context.close();
    }

    private static void successCase(SpringAccountTransferService service) {
        service.transfer("A", "B", 300);

        System.out.println("[success] A.balance = " + service.getBalance("A") + " (기대 700)");
        System.out.println("[success] B.balance = " + service.getBalance("B") + " (기대 800)");
    }

    private static void failureCase(SpringAccountTransferService service) {
        try {
            service.transfer("C", "broken", 300);
        } catch (IllegalStateException e) {
            System.out.println("[failure] 예외 발생: " + e.getMessage());
        }

        System.out.println("[failure] C.balance = " + service.getBalance("C") + " (기대 1000, DB가 자동 rollback)");
        System.out.println("[failure] broken.balance = " + service.getBalance("broken") + " (기대 500)");
    }
}
