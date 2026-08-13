package com.deepspring.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ConfigurationProxyExperiment {

    // Full Configuration (default): proxyBeanMethods = true
    @Configuration
    static class FullConfig {
        @Bean
        public String beanA() {
            return new String("A");
        }

        @Bean
        public String beanB() {
            // 메서드 안에서 다른 @Bean 메서드를 "직접 호출"
            return beanA() + beanA();
        }
    }

    // Lite Configuration: proxyBeanMethods = false
    @Configuration(proxyBeanMethods = false)
    static class LiteConfig {
        @Bean
        public String beanA() {
            return new String("A");
        }

        @Bean
        public String beanB() {
            return beanA() + beanA();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Full Configuration (proxyBeanMethods=true) ===");
        try (AnnotationConfigApplicationContext full = new AnnotationConfigApplicationContext(FullConfig.class)) {
            FullConfig config = full.getBean(FullConfig.class);
            System.out.println("Config class = " + config.getClass());
            String direct1 = config.beanA();
            String direct2 = config.beanA();
            String containerBean = full.getBean("beanA", String.class);
            System.out.println("config.beanA() == config.beanA() ? " + (direct1 == direct2));
            System.out.println("config.beanA() == container's beanA bean ? " + (direct1 == containerBean));
        }

        System.out.println();
        System.out.println("=== Lite Configuration (proxyBeanMethods=false) ===");
        try (AnnotationConfigApplicationContext lite = new AnnotationConfigApplicationContext(LiteConfig.class)) {
            LiteConfig config = lite.getBean(LiteConfig.class);
            System.out.println("Config class = " + config.getClass());
            String direct1 = config.beanA();
            String direct2 = config.beanA();
            String containerBean = lite.getBean("beanA", String.class);
            System.out.println("config.beanA() == config.beanA() ? " + (direct1 == direct2));
            System.out.println("config.beanA() == container's beanA bean ? " + (direct1 == containerBean));
        }
    }
}
