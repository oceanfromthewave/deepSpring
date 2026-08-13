package com.deepspring.springmvc;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringMvcExperiment
{
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
		appContext.register(WebConfig.class);

		Tomcat tomcat = new Tomcat();
		tomcat.setPort(18080);
		tomcat.getConnector();

		Context context = tomcat.addContext("", null);
		DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
		Tomcat.addServlet(context, "dispatcherServlet", dispatcherServlet).setLoadOnStartup(1);
		context.addServletMappingDecoded("/", "dispatcherServlet");

		FilterDef filterDef = new FilterDef();
		filterDef.setFilterName("loggingFilter");
		filterDef.setFilter(new com.deepspring.servlet.LoggingFilter());
		context.addFilterDef(filterDef);

		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName("loggingFilter");
		filterMap.addURLPattern("/*");
		context.addFilterMap(filterMap);

		tomcat.start();
		System.out.println("Spring MVC Tomcat started: http://localhost:18080/greet?name=spring");
		tomcat.getServer().await();
	}
}