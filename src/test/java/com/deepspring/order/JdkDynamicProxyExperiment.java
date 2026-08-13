package com.deepspring.order;

import com.deepspring.member.MemoryMemberRepository;

import java.lang.reflect.Proxy;

public class JdkDynamicProxyExperiment {

    public static void main(String[] args) {
        OrderServiceInterface real = new OrderService(new MemoryMemberRepository(), new FixDiscountPolicy());

        OrderServiceInterface proxy = (OrderServiceInterface) Proxy.newProxyInstance(
                OrderServiceInterface.class.getClassLoader(),
                new Class[]{OrderServiceInterface.class},
                new TimeLoggingInvocationHandler(real)
        );

        System.out.println("proxy.getClass() = " + proxy.getClass());
        System.out.println("proxy instanceof OrderServiceInterface? " + (proxy instanceof OrderServiceInterface));
        System.out.println("proxy instanceof OrderService? " + (proxy instanceof OrderService));

        int result = proxy.calculatePrice(1L, 10000);
        System.out.println("result = " + result);
    }
}
