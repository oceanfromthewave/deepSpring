package com.deepspring.order;

import com.deepspring.member.MemoryMemberRepository;
import org.springframework.cglib.proxy.Enhancer;

public class CglibProxyExperiment {

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(OrderService.class); // target 없음, OrderService 자체를 상속
        enhancer.setCallback(new TimeLoggingMethodInterceptor());

        OrderService proxy = (OrderService) enhancer.create(
                new Class[]{com.deepspring.member.MemberRepository.class, com.deepspring.order.DiscountPolicy.class},
                new Object[]{new MemoryMemberRepository(), new FixDiscountPolicy()}
        );

        System.out.println("proxy.getClass() = " + proxy.getClass());
        System.out.println("proxy instanceof OrderService? " + (proxy instanceof OrderService));
        System.out.println("proxy instanceof OrderServiceInterface? " + (proxy instanceof OrderServiceInterface));

        int result = proxy.calculatePrice(1L, 10000);
        System.out.println("result = " + result);
    }
}
