package com.deepspring.transaction;

import java.lang.reflect.Proxy;

public class ProxyTransactionExperiment {

    public static void main(String[] args) {
        directCallFails();
        successCase();
        failureCase();
    }

    // Proxy 없이 직접 호출 → Connection 바인딩 안 돼있어서 터짐
    private static void directCallFails() {
        AccountTransferService target = new AccountTransferService();
        Account a = new Account("A", 1000);
        Account b = new Account("B", 500);

        try {
            target.transfer(a, b, 300);
            System.out.println("[direct] 예상과 다름: 예외 없이 성공함");
        } catch (IllegalStateException e) {
            System.out.println("[direct] 예상대로 실패: " + e.getMessage());
        }
    }

    private static AccountTransferServiceInterface createProxy() {
        AccountTransferService target = new AccountTransferService();
        return (AccountTransferServiceInterface) Proxy.newProxyInstance(
                AccountTransferServiceInterface.class.getClassLoader(),
                new Class[]{AccountTransferServiceInterface.class},
                new TransactionInvocationHandler(target)
        );
    }

    // Proxy 경유: 자동으로 begin/commit
    private static void successCase() {
        AccountTransferServiceInterface proxy = createProxy();
        Account a = new Account("A", 1000);
        Account b = new Account("B", 500);

        proxy.transfer(a, b, 300);

        System.out.println("[proxy-success] A.balance = " + a.getBalance() + " (기대 700)");
        System.out.println("[proxy-success] B.balance = " + b.getBalance() + " (기대 800)");
    }

    // Proxy 경유: 자동으로 rollback
    private static void failureCase() {
        AccountTransferServiceInterface proxy = createProxy();
        Account a = new Account("A", 1000);
        Account broken = new Account("broken", 500);

        try {
            proxy.transfer(a, broken, 300);
        } catch (IllegalStateException e) {
            System.out.println("[proxy-failure] 예외 발생: " + e.getMessage());
        }

        System.out.println("[proxy-failure] A.balance = " + a.getBalance() + " (기대 1000, 자동 rollback)");
        System.out.println("[proxy-failure] broken.balance = " + broken.getBalance() + " (기대 500)");
    }
}
