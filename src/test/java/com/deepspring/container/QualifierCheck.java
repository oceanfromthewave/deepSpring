package com.deepspring.container;

import com.deepspring.member.MemoryMemberRepository;
import com.deepspring.order.FixDiscountPolicy;
import com.deepspring.order.OrderService;
import com.deepspring.order.RateDiscountPolicy;
import java.lang.reflect.Field;

public class QualifierCheck {

    public static void main(String[] args) throws Exception {
        SimpleApplicationContext context = new SimpleApplicationContext();
        context.registerBeanDefinition("memberRepository", MemoryMemberRepository.class);
        context.registerBeanDefinition("fixDiscountPolicy", FixDiscountPolicy.class);
        context.registerBeanDefinition("rateDiscountPolicy", RateDiscountPolicy.class);
        context.registerBeanDefinition("orderService", OrderService.class);

        OrderService orderService = (OrderService) context.getBean("orderService");
        Field field = OrderService.class.getDeclaredField("discountPolicy");
        field.setAccessible(true);
        System.out.println("actual discountPolicy class = " + field.get(orderService).getClass().getSimpleName());
        System.out.println("expected: FixDiscountPolicy (Qualifier가 Primary(Rate) 이겨야 함)");
    }
}
