package com.deepspring.config;

import com.deepspring.member.MemberRepository;
import com.deepspring.order.OrderService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RealSpringExperiment {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(SpringAppConfig.class);

        System.out.println("=== 등록된 Bean 목록 ===");
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println(" - " + name);
        }

        OrderService orderService = context.getBean(OrderService.class);
        System.out.println("vipPrice = " + orderService.calculatePrice(1L, 10000));

        // Proxy 캐싱(=싱글톤) 확인: memberRepository() 두 번 호출해도 같은 인스턴스인지
        MemberRepository first = context.getBean(MemberRepository.class);
        MemberRepository second = context.getBean(MemberRepository.class);
        System.out.println("memberRepository singleton? " + (first == second));

        // SpringAppConfig 자체가 CGLIB Proxy로 감싸졌는지 확인
        SpringAppConfig config = context.getBean(SpringAppConfig.class);
        System.out.println("SpringAppConfig class = " + config.getClass());
        System.out.println("CGLIB Proxy? " + config.getClass().getName().contains("$$"));

        context.close();
    }
}
