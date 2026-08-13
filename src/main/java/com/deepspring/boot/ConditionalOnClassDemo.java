package com.deepspring.boot;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot Auto Configuration의 핵심 트릭: @ConditionalOnClass 흉내. "classpath에 이 클래스 있으면 Bean 등록, 없으면 skip."
 */
public class ConditionalOnClassDemo
{

	// Bean 저장소 (SimpleApplicationContext의 미니 버전)
	static Map<String, Object> registry = new HashMap<>();

	public static void main(String[] args)
	{
		registerTomcatIfPresent();
		registerJettyIfPresent();

		System.out.println("등록된 Bean: " + registry.keySet());
	}

	// classpath에 Tomcat embed 있을 때만 동작 (지금 build.gradle에 있음 → 등록됨)
	static void registerTomcatIfPresent()
	{
		if(isPresent("org.apache.catalina.startup.Tomcat"))
		{
			registry.put("tomcatServletWebServerFactory", new Object());
			System.out.println("[ON] Tomcat 발견 → TomcatServletWebServerFactory 등록");
		}
		else
		{
			System.out.println("[OFF] Tomcat 없음 → skip");
		}
	}

	// classpath에 Jetty 없음 (build.gradle에 추가한 적 없음) → skip 확인용
	static void registerJettyIfPresent()
	{
		if(isPresent("org.eclipse.jetty.server.Server"))
		{
			registry.put("jettyServletWebServerFactory", new Object());
			System.out.println("[ON] Jetty 발견 → JettyServletWebServerFactory 등록");
		}
		else
		{
			System.out.println("[OFF] Jetty 없음 → skip");
		}
	}

	// Spring의 ClassUtils.isPresent()가 실제로 이렇게 동작함
	static boolean isPresent(String className)
	{
		try
		{
			Class.forName(className);
			return true;
		}
		catch(ClassNotFoundException e)
		{
			return false;
		}
	}
}
