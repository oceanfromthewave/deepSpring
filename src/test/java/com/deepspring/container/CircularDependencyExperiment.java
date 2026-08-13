package com.deepspring.container;

public class CircularDependencyExperiment {

    public static void main(String[] args) {
        SimpleApplicationContext context = new SimpleApplicationContext();
        context.registerBeanDefinition("a", CircularA.class);
        context.registerBeanDefinition("b", CircularB.class);

        try {
            context.getBean("a");
            System.out.println("생성 성공 (예상 밖)");
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError 발생 (예상대로 순환 의존성 못 품)");
        }
    }
}
