package com.deepspring.container;

import com.deepspring.member.MemberRepository;
import com.deepspring.member.MemoryMemberRepository;
import com.deepspring.order.DiscountPolicy;
import com.deepspring.order.OrderService;
import com.deepspring.order.RateDiscountPolicy;

public class SimpleApplicationContextExperiment {

    public static void main(String[] args) {
        SimpleApplicationContext context = new SimpleApplicationContext();

        // 등록: 이름 + 타입만 알려줌. 어떻게 조립할지는 알려주지 않음.
        context.registerBeanDefinition("memberRepository", MemoryMemberRepository.class);
        context.registerBeanDefinition("discountPolicy", RateDiscountPolicy.class);
        context.registerBeanDefinition("orderService", OrderService.class);

        // 조회: Container가 생성자 리플렉션으로 알아서 의존관계 연결.
        OrderService orderService = (OrderService) context.getBean("orderService");

        int vipPrice = orderService.calculatePrice(1L, 10000);
        System.out.println("vipPrice = " + vipPrice);

        // 싱글톤 확인
        Object repo1 = context.getBean("memberRepository");
        Object repo2 = context.getBean("memberRepository");
        System.out.println("memberRepository singleton? " + (repo1 == repo2));

        MemberRepository directCastCheck = (MemberRepository) repo1;
        System.out.println("class = " + directCastCheck.getClass());

        DiscountPolicy policy = (DiscountPolicy) context.getBean("discountPolicy");
        System.out.println("policy class = " + policy.getClass());
    }
}
