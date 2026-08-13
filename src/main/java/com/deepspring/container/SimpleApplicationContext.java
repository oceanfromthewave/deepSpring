package com.deepspring.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleApplicationContext
{

	private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
	private final Map<String, Object> singletonObjects = new HashMap<>();
	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

	public SimpleApplicationContext()
	{
		addBeanPostProcessor(new BeanPostProcessor()
		{
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName)
			{
				try
				{
					invokePostConstruct(bean);
				}
				catch(Exception e)
				{
					throw new IllegalStateException("post construct failed: " + beanName, e);
				}
				return bean;
			}
		});
	}

	public void addBeanPostProcessor(BeanPostProcessor processor)
	{
		beanPostProcessors.add(processor);
	}

	public void registerBeanDefinition(String beanName, Class<?> beanClass)
	{
		beanDefinitions.put(beanName, new BeanDefinition(beanName, beanClass));
	}

	public Object getBean(String beanName)
	{
		Object cached = singletonObjects.get(beanName);
		if(cached != null)
		{
			return cached;
		}

		BeanDefinition beanDefinition = beanDefinitions.get(beanName);
		if(beanDefinition == null)
		{
			throw new IllegalStateException("no bean named '" + beanName + "'");
		}

		Object bean = createBean(beanDefinition);
		singletonObjects.put(beanName, bean);
		return bean;
	}

	private Object createBean(BeanDefinition beanDefinition)
	{
		try
		{
			Class<?> beanClass = beanDefinition.getBeanClass();
			Constructor<?> constructor = beanClass.getConstructors()[0];
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

			Object[] arguments = new Object[parameterTypes.length];
			for(int i = 0; i < parameterTypes.length; i++)
			{
				arguments[i] = getBeanByType(parameterTypes[i], findQualifier(parameterAnnotations[i]));
			}

			Object bean = constructor.newInstance(arguments);

			for(BeanPostProcessor processor : beanPostProcessors)
			{
				bean = processor.postProcessBeforeInitialization(bean, beanDefinition.getBeanName());
			}
			for(BeanPostProcessor processor : beanPostProcessors)
			{
				bean = processor.postProcessAfterInitialization(bean, beanDefinition.getBeanName());
			}

			return bean;
		}
		catch(Exception e)
		{
			throw new IllegalStateException("bean creation failed: " + beanDefinition.getBeanName(), e);
		}
	}

	private void invokePostConstruct(Object bean) throws Exception
	{
		for(Method method : bean.getClass().getDeclaredMethods())
		{
			if(method.isAnnotationPresent(PostConstruct.class))
			{
				method.setAccessible(true);
				method.invoke(bean);
			}
		}
	}

	private String findQualifier(Annotation[] annotations)
	{
		for(Annotation annotation : annotations)
		{
			if(annotation instanceof Qualifier)
			{
				return ((Qualifier) annotation).value();
			}
		}
		return null;
	}

	private Object getBeanByType(Class<?> type, String qualifier)
	{
		List<BeanDefinition> candidates = new ArrayList<>();
		for(BeanDefinition beanDefinition : beanDefinitions.values())
		{
			if(type.isAssignableFrom(beanDefinition.getBeanClass()))
			{
				candidates.add(beanDefinition);
			}
		}

		if(candidates.isEmpty())
		{
			throw new IllegalStateException("no bean of type " + type.getName());
		}
		if(candidates.size() == 1)
		{
			return getBean(candidates.get(0).getBeanName());
		}

		if(qualifier != null)
		{
			for(BeanDefinition candidate : candidates)
			{
				if(candidate.getBeanName().equals(qualifier))
				{
					return getBean(candidate.getBeanName());
				}
			}
			throw new IllegalStateException("no bean named '" + qualifier + "' matches qualifier for type " + type.getName());
		}

		List<BeanDefinition> primaryCandidates = new ArrayList<>();
		for(BeanDefinition candidate : candidates)
		{
			if(candidate.getBeanClass().isAnnotationPresent(Primary.class))
			{
				primaryCandidates.add(candidate);
			}
		}

		if(primaryCandidates.size() == 1)
		{
			return getBean(primaryCandidates.get(0).getBeanName());
		}

		throw new IllegalStateException("no unique bean of type " + type.getName() + ", found " + candidates.size() + " candidates");
	}
}
