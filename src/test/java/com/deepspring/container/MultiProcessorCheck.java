package com.deepspring.container;

import com.deepspring.member.MemoryMemberRepository;

public class MultiProcessorCheck {
    public static void main(String[] args) {
        SimpleApplicationContext context = new SimpleApplicationContext();
        context.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                System.out.println("[before] " + beanName);
                return bean;
            }
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                System.out.println("[after] " + beanName);
                return bean;
            }
        });
        context.registerBeanDefinition("memberRepository", MemoryMemberRepository.class);
        context.getBean("memberRepository");
    }
}
