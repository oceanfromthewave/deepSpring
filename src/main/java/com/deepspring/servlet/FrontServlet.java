package com.deepspring.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class FrontServlet extends HttpServlet
{
	private final Map<String, Object> controllerMapping = new HashMap<>();
	private final Map<String, Method> handlerMapping = new HashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public FrontServlet()
	{
		register(new HelloController());
	}

	private void register(Object controller)
	{
		for(Method method : controller.getClass().getDeclaredMethods())
		{
			GetMapping annotation = method.getAnnotation(GetMapping.class);
			if(annotation == null)
			{
				continue;
			}
			String path = annotation.value();
			controllerMapping.put(path, controller);
			handlerMapping.put(path, method);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String uri = req.getRequestURI();
		Method method = handlerMapping.get(uri);
		Object controller = controllerMapping.get(uri);

		if(method == null)
		{
			resp.setStatus(404);
			return;
		}

		Object[] args = resolveArguments(method, req);

		try
		{
			Object result = method.invoke(controller, args);
			writeResponse(result, resp);
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			resp.setStatus(500);
		}
	}

	private void writeResponse(Object result, HttpServletResponse resp) throws IOException
	{
		if(result instanceof String)
		{
			resp.setContentType("text/plain;charset=UTF-8");
			resp.getWriter().write((String) result);
			return;
		}

		resp.setContentType("application/json;charset=UTF-8");
		resp.getWriter().write(objectMapper.writeValueAsString(result));
	}

	private Object[] resolveArguments(Method method, HttpServletRequest req)
	{
		Parameter[] parameters = method.getParameters();
		Object[] args = new Object[parameters.length];

		for(int i = 0; i < parameters.length; i++)
		{
			RequestParam annotation = parameters[i].getAnnotation(RequestParam.class);
			if(annotation != null)
			{
				args[i] = req.getParameter(annotation.value());
			}
		}

		return args;
	}

}
