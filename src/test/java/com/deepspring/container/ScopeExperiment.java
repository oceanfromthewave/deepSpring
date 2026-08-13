package com.deepspring.container;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

public class ScopeExperiment {

    @Component
    @Scope("prototype")
    static class PrototypeBean {
        private static final AtomicInteger COUNTER = new AtomicInteger();
        private final int id = COUNTER.incrementAndGet();

        public int getId() {
            return id;
        }
    }

    @Component // scope 지정 없음 -> 기본 singleton
    static class SingletonHolder {
        private final PrototypeBean prototypeBean;

        public SingletonHolder(PrototypeBean prototypeBean) {
            this.prototypeBean = prototypeBean;
        }

        public PrototypeBean getPrototypeBean() {
            return prototypeBean;
        }
    }

    @Component
    static class SingletonHolderWithProvider {
        private final ObjectProvider<PrototypeBean> prototypeBeanProvider;

        public SingletonHolderWithProvider(ObjectProvider<PrototypeBean> prototypeBeanProvider) {
            this.prototypeBeanProvider = prototypeBeanProvider;
        }

        public PrototypeBean freshPrototypeBean() {
            return prototypeBeanProvider.getObject(); // 호출할 때마다 컨테이너에 새로 요청
        }
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(PrototypeBean.class, SingletonHolder.class, SingletonHolderWithProvider.class);
            context.refresh();

            System.out.println("=== 1. PrototypeBean 직접 조회 (매번 다른 인스턴스?) ===");
            PrototypeBean p1 = context.getBean(PrototypeBean.class);
            PrototypeBean p2 = context.getBean(PrototypeBean.class);
            System.out.println("p1.id=" + p1.getId() + ", p2.id=" + p2.getId() + ", p1==p2? " + (p1 == p2));

            System.out.println();
            System.out.println("=== 2. SingletonHolder 자체는 싱글톤인가? ===");
            SingletonHolder h1 = context.getBean(SingletonHolder.class);
            SingletonHolder h2 = context.getBean(SingletonHolder.class);
            System.out.println("h1==h2? " + (h1 == h2));

            System.out.println();
            System.out.println("=== 3. SingletonHolder가 물고 있는 PrototypeBean, 매번 새 걸로 바뀌나? ===");
            System.out.println("h1이 물고 있는 prototype.id=" + h1.getPrototypeBean().getId());
            System.out.println("h1이 물고 있는 prototype.id=" + h1.getPrototypeBean().getId() + " (다시 조회)");
            System.out.println("직접 조회한 새 prototype.id=" + context.getBean(PrototypeBean.class).getId());

            System.out.println();
            System.out.println("=== 4. ObjectProvider로 고친 버전 ===");
            SingletonHolderWithProvider hp = context.getBean(SingletonHolderWithProvider.class);
            System.out.println("freshPrototypeBean().id=" + hp.freshPrototypeBean().getId());
            System.out.println("freshPrototypeBean().id=" + hp.freshPrototypeBean().getId());
            System.out.println("freshPrototypeBean().id=" + hp.freshPrototypeBean().getId());
        }
    }
}
