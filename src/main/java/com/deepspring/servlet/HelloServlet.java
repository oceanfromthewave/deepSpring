package com.deepspring.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HelloServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		String name = req.getParameter("name");
		if(name == null)
		{
			name = "world";
		}

		resp.setContentType("text/plain;charset=UTF-8");
		resp.getWriter()
				.write("hello, " + name + " / method=" + req.getMethod() + " / uri=" + req.getRequestURI() + " / thread=" + Thread.currentThread().getName());
	}
}
