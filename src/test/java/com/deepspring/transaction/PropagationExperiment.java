package com.deepspring.transaction;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.UnexpectedRollbackException;

public class PropagationExperiment {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TxConfig.class);

        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        PropagationServiceA serviceA = context.getBean(PropagationServiceA.class);

        requiredCase(serviceA, jdbcTemplate);
        requiresNewCase(serviceA, jdbcTemplate);

        context.close();
    }

    private static void requiredCase(PropagationServiceA serviceA, JdbcTemplate jdbcTemplate) {
        System.out.println("[REQUIRED] callWithRequired(D, E, 100) 호출");
        try {
            serviceA.callWithRequired("D", "E", 100);
        } catch (UnexpectedRollbackException e) {
            System.out.println("  A 쪽에서 UnexpectedRollbackException 발생: " + e.getMessage());
        }

        System.out.println("[REQUIRED] D.balance = " + query(jdbcTemplate, "D") + " (기대 1000, 전체 롤백)");
        System.out.println("[REQUIRED] E.balance = " + query(jdbcTemplate, "E") + " (기대 1000, 전체 롤백)");
    }

    private static void requiresNewCase(PropagationServiceA serviceA, JdbcTemplate jdbcTemplate) {
        System.out.println("[REQUIRES_NEW] callWithRequiresNew(F, G, 100) 호출");
        serviceA.callWithRequiresNew("F", "G", 100);

        System.out.println("[REQUIRES_NEW] F.balance = " + query(jdbcTemplate, "F") + " (기대 1100, A는 커밋됨)");
        System.out.println("[REQUIRES_NEW] G.balance = " + query(jdbcTemplate, "G") + " (기대 1000, B만 롤백)");
    }

    private static int query(JdbcTemplate jdbcTemplate, String name) {
        return jdbcTemplate.queryForObject("SELECT balance FROM account WHERE name = ?", Integer.class, name);
    }
}
