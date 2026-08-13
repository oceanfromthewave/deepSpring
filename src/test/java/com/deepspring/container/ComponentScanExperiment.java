package com.deepspring.container;

import com.deepspring.order.OrderService;
import java.util.List;

public class ComponentScanExperiment {

    public static void main(String[] args) {
        ComponentScanner scanner = new ComponentScanner();
        List<Class<?>> found = scanner.scan("com.deepspring");

        System.out.println("발견된 @Component 클래스:");
        for (Class<?> clazz : found) {
            System.out.println(" - " + clazz.getName());
        }

        SimpleApplicationContext context = new SimpleApplicationContext();
        for (Class<?> clazz : found) {
            String beanName = Character.toLowerCase(clazz.getSimpleName().charAt(0))
                    + clazz.getSimpleName().substring(1);
            context.registerBeanDefinition(beanName, clazz);
        }

        OrderService orderService = (OrderService) context.getBean("orderService");
        int vipPrice = orderService.calculatePrice(1L, 10000);
        System.out.println("vipPrice = " + vipPrice);
    }
}
