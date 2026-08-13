package com.deepspring.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class LoggingFilter implements Filter
{
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		long start = System.currentTimeMillis();

		System.out.println("[Filter] before: " + req.getRequestURI());
		chain.doFilter(request, response);
		System.out.println("[Filter] after: " + req.getRequestURI() + " (" + (System.currentTimeMillis() - start) + "ms)");
	}
}
