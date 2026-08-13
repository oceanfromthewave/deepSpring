package com.deepspring.container;

import com.deepspring.order.DiscountPolicy;
import com.deepspring.order.FixDiscountPolicy;
import com.deepspring.order.OrderService;
import com.deepspring.order.RateDiscountPolicy;

public class AmbiguousBeanExperiment {

    public static void main(String[] args) {
        SimpleApplicationContext context = new SimpleApplicationContext();
        context.registerBeanDefinition("memberRepository", com.deepspring.member.MemoryMemberRepository.class);
        context.registerBeanDefinition("fixDiscountPolicy", FixDiscountPolicy.class);
        context.registerBeanDefinition("rateDiscountPolicy", RateDiscountPolicy.class);
        context.registerBeanDefinition("orderService", OrderService.class);

        // DiscountPolicy 타입 빈이 2개(Fix, Rate) 등록된 상태.
        // getBeanByType()은 HashMap을 순회하며 "처음 맞는 것"을 반환한다.
        // 어떤 게 선택될지 호출자는 알 수 없고, 컨테이너도 에러를 내지 않는다.
        OrderService orderService = (OrderService) context.getBean("orderService");
        int vipPrice = orderService.calculatePrice(1L, 10000);
        System.out.println("생성 성공 (예외 없음). vipPrice = " + vipPrice + " <- Fix 정책(9000)인지 Rate 정책(9000, 우연히 같음)인지 코드만 봐선 알 수 없다");
    }
}
