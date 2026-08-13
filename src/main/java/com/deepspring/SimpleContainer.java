package com.deepspring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleContainer
{
	private final Map<Class<?>, Object> cache = new HashMap<>();

	public <T> T getBean(Class<T> type, Supplier<T> creator)
	{
		Object cached = cache.get(type);
		if(cached != null)
		{
			return type.cast(cached);
		}
		T created = creator.get();
		cache.put(type, created);
		return created;
	}
}
