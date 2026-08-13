package com.deepspring.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

public class ServletExperiment
{
	public static void main(String[] args) throws Exception
	{
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(18080);
		tomcat.getConnector();

		Context context = tomcat.addContext("", null);
		tomcat.addServlet(context, "frontServlet", new FrontServlet());
		context.addServletMappingDecoded("/*", "frontServlet");

		FilterDef filterDef = new FilterDef();
		filterDef.setFilterName("loggingFilter");
		filterDef.setFilter(new LoggingFilter());
		context.addFilterDef(filterDef);

		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName("loggingFilter");
		filterMap.addURLPattern("/*");
		context.addFilterMap(filterMap);

		tomcat.start();
		System.out.println("Tomcat stated: http://localhost:8080/hello?name=spring");
		tomcat.getServer().await();
	}
}
