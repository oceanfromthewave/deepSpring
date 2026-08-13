package com.deepspring.order;

import com.deepspring.member.MemoryMemberRepository;

public class ManualWrapperExperiment {

    public static void main(String[] args) {
        OrderServiceInterface real = new OrderService(new MemoryMemberRepository(), new FixDiscountPolicy());
        OrderServiceInterface wrapped = new OrderServiceTimeLoggingWrapper(real);

        int result = wrapped.calculatePrice(1L, 10000);
        System.out.println("result = " + result);
    }
}
