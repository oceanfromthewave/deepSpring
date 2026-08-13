package com.deepspring;

import com.deepspring.member.MemberRepository;

public class SingletonExperiment {

    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();

        MemberRepository first = appConfig.memberRepository();
        MemberRepository second = appConfig.memberRepository();

        System.out.println("first  = " + first);
        System.out.println("second = " + second);
        System.out.println("same instance? " + (first == second));

        // orderService()도 매번 새 조립.
        Object order1 = appConfig.orderService();
        Object order2 = appConfig.orderService();
        System.out.println("orderService same instance? " + (order1 == order2));
    }
}
