package com.deepspring.integration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.util.concurrent.CountDownLatch;

public class CacheExperiment
{
	@Configuration
	@ComponentScan(basePackages = "com.deepspring.integration",
			excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
					classes = {PaymentService.class, PaymentRepository.class}))
	@EnableCaching
	static class CacheConfig
	{
		@Bean
		public CacheManager cacheManager()
		{
			return new ConcurrentMapCacheManager("slowApi");
		}
	}

	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CacheConfig.class);
		CachedApiService service = context.getBean(CachedApiService.class);

		System.out.println("=== 1. 첫 호출 (캐시 miss) ===");
		time(() -> service.fetch("a"));

		System.out.println();
		System.out.println("=== 2. 두번째 호출, 같은 키 (캐시 hit) ===");
		time(() -> service.fetch("a"));

		System.out.println();
		System.out.println("=== 3. 다른 키 (캐시 miss) ===");
		time(() -> service.fetch("b"));

		System.out.println();
		System.out.println("=== 4. Cache Stampede: 새 키로 동시 3건, sync=false ===");
		runConcurrent(3, () -> service.fetch("stampede"));

		System.out.println();
		System.out.println("=== 5. 같은 조건, sync=true ===");
		runConcurrent(3, () -> service.fetchSync("stampede-sync"));

		context.close();
	}

	private static void time(Runnable task)
	{
		long start = System.currentTimeMillis();
		task.run();
		System.out.println("    걸린 시간 = " + (System.currentTimeMillis() - start) + "ms");
	}

	private static void runConcurrent(int count, Runnable task) throws Exception
	{
		CountDownLatch ready = new CountDownLatch(count);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(count);

		for (int i = 0; i < count; i++)
		{
			new Thread(() ->
			{
				ready.countDown();
				try
				{
					start.await();
					task.run();
				}
				catch (Exception e)
				{
					System.out.println("    실패: " + e.getClass().getSimpleName());
				}
				finally
				{
					done.countDown();
				}
			}, "worker-" + i).start();
		}

		ready.await();
		long begin = System.currentTimeMillis();
		start.countDown();
		done.await();
		System.out.println("    전체 걸린 시간 = " + (System.currentTimeMillis() - begin) + "ms");
	}
}
