package com.deepspring.boot;

import java.util.HashMap;
import java.util.Map;

/**
 * @ConditionalOnMissingBean 흉내: "이미 등록된 Bean 있으면 auto-config는 양보한다."
 */
public class ConditionalOnMissingBeanDemo
{

	static Map<String, Object> registry = new HashMap<>();

	public static void main(String[] args)
	{
		runScenario("사용자 설정 없음", false);

		registry.clear();

		runScenario("사용자가 dataSource 직접 등록", true);
	}

	static void runScenario(String label, boolean userDefinesDataSource)
	{
		System.out.println("=== " + label + " ===");

		// 1단계: 사용자 @Configuration 먼저 처리 (Spring이 항상 이 순서로 함)
		if(userDefinesDataSource)
		{
			registry.put("dataSource", "UserDefinedHikariDataSource");
			System.out.println("[USER] dataSource 등록");
		}

		// 2단계: Auto Configuration은 나중에, 이미 있으면 양보
		registerDataSourceIfMissing();

		System.out.println("최종 dataSource = " + registry.get("dataSource"));
	}

	static void registerDataSourceIfMissing()
	{
		if(!registry.containsKey("dataSource"))
		{
			registry.put("dataSource", "AutoConfiguredHikariDataSource");
			System.out.println("[AUTO] dataSource 없어서 기본값 등록");
		}
		else
		{
			System.out.println("[AUTO] dataSource 이미 있음 → skip");
		}
	}
}
