package com.deepspring.order;

import com.deepspring.AppConfig;

public class OrderServiceTest {

    public static void main(String[] args) {
        // 조립 책임: AppConfig 하나로 집중.
        AppConfig appConfig = new AppConfig();
        OrderService orderService = appConfig.orderService();

        int vipPrice = orderService.calculatePrice(1L, 10000);
        assertEquals("VIP FixDiscountPolicy (10000 - 1000)", 9000, vipPrice);

        int basicPrice = orderService.calculatePrice(2L, 10000);
        assertEquals("BASIC FixDiscountPolicy (할인 없음)", 10000, basicPrice);

        System.out.println("OrderServiceTest: 전부 통과");
    }

    private static void assertEquals(String label, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(label + " 실패: expected=" + expected + " actual=" + actual);
        }
        System.out.println(label + " 통과 (" + actual + ")");
    }
}
