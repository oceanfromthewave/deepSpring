package com.deepspring.integration;

public class TimeoutExperiment
{
	public static void main(String[] args)
	{
		ExternalApiClient client = new ExternalApiClient();

		System.out.println("=== 1. 타임아웃 설정 있음 (read 2초, 서버는 5초 걸림) ===");
		long start = System.currentTimeMillis();
		try
		{
			System.out.println("응답 = " + client.callWithTimeout());
		}
		catch (Exception e)
		{
			System.out.println("실패: " + e.getClass().getSimpleName());
			Throwable cause = e.getCause();
			while (cause != null)
			{
				System.out.println("  cause: " + cause.getClass().getName() + " - " + cause.getMessage());
				cause = cause.getCause();
			}
		}
		System.out.println("걸린 시간 = " + (System.currentTimeMillis() - start) + "ms");

		System.out.println();
		System.out.println("=== 2. 타임아웃 설정 없음 (무한 대기) ===");
		start = System.currentTimeMillis();
		try
		{
			System.out.println("응답 = " + client.callWithoutTimeout());
		}
		catch (Exception e)
		{
			System.out.println("실패: " + e.getClass().getSimpleName());
		}
		System.out.println("걸린 시간 = " + (System.currentTimeMillis() - start) + "ms");
	}
}
