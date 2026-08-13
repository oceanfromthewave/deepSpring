package com.deepspring.container;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ComponentScanner
{

	public List<Class<?>> scan(String basePackage)
	{
		List<Class<?>> result = new ArrayList<>();
		String path = basePackage.replace('.', '/');
		URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
		if(resource == null)
		{
			return result;
		}
		File directory = new File(resource.getFile());
		scanDirectory(directory, basePackage, result);
		return result;
	}

	private void scanDirectory(File directory, String packageName, List<Class<?>> result)
	{
		File[] files = directory.listFiles();
		if(files == null)
		{
			return;
		}
		for(File file : files)
		{
			if(file.isDirectory())
			{
				scanDirectory(file, packageName + "." + file.getName(), result);
				continue;
			}
			if(!file.getName().endsWith(".class"))
			{
				continue;
			}
			String simpleName = file.getName().substring(0, file.getName().length() - ".class".length());
			String className = packageName + "." + simpleName;
			loadIfComponent(className, result);
		}
	}

	private void loadIfComponent(String className, List<Class<?>> result)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if(clazz.isAnnotationPresent(Component.class))
			{
				result.add(clazz);
			}
		}
		catch(ClassNotFoundException e)
		{
			throw new IllegalStateException("class load failed: " + className, e);
		}
	}
}
