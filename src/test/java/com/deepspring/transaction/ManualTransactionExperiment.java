package com.deepspring.transaction;

public class ManualTransactionExperiment {

    public static void main(String[] args) {
        successCase();
        failureCase();
    }

    // 정상 이체: commit 경로
    private static void successCase() {
        Account a = new Account("A", 1000);
        Account b = new Account("B", 500);

        new AccountTransferService().transfer(a, b, 300);

        System.out.println("[success] A.balance = " + a.getBalance() + " (기대 700)");
        System.out.println("[success] B.balance = " + b.getBalance() + " (기대 800)");
    }

    // 이체 도중 예외 발생: rollback 경로
    private static void failureCase() {
        Account a = new Account("A", 1000);
        Account broken = new Account("broken", 500);

        try {
            new AccountTransferService().transfer(a, broken, 300);
        } catch (IllegalStateException e) {
            System.out.println("[failure] 예외 발생: " + e.getMessage());
        }

        // rollback 안 됐으면 A.balance == 700 (깨진 상태)
        // rollback 됐으면 A.balance == 1000 (원상복구)
        System.out.println("[failure] A.balance = " + a.getBalance() + " (기대 1000, rollback 성공 시)");
        System.out.println("[failure] broken.balance = " + broken.getBalance() + " (기대 500)");
    }
}
