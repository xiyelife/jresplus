/*
 * 2013-8-13	STORY #6532 [研发中心/童世红][TS:201308080002][jresplus/mvc]-无法加载其他配置的SpringBean
 * 2014-2-8		STORY #7561 [研发中心/内部需求][jresplus-mvc]spring bean定义加载默认支持common包和mvc中的配置
 */
package com.hundsun.jresplus.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * @author LeoHu copy by sagahl
 * 
 */
public class ContextLoaderListener extends
		org.springframework.web.context.ContextLoaderListener implements
		ServletContextListener {
	public static final String CONFIG_LOCATION = "JresConfigLocation";

	public void contextInitialized(ServletContextEvent event) {
		String location = event.getServletContext().getInitParameter(
				CONFIG_LOCATION);
		System.setProperty(CONFIG_LOCATION, location);
		super.contextInitialized(event);
	}

	protected void customizeContext(ServletContext servletContext,
			ConfigurableWebApplicationContext applicationContext) {
		String[] configLocations = applicationContext.getConfigLocations();
		String[] jresConfigLocations = null;
		if (configLocations == null || configLocations.length < 1) {
			configLocations = StringUtils.tokenizeToStringArray(
					servletContext.getInitParameter(CONFIG_LOCATION_PARAM),
					",; \t\n");
		}
		if (configLocations != null) {
			jresConfigLocations = new String[configLocations.length + 2];
			jresConfigLocations[0] = "classpath:conf/spring/jres-web-beans.xml";
			jresConfigLocations[1] = "classpath:conf/spring/jres-common-beans.xml";
			int index = 2;
			for (String config : configLocations) {
				jresConfigLocations[index] = config;
				index++;
			}

		}
		applicationContext.setConfigLocations(jresConfigLocations);
		super.customizeContext(servletContext, applicationContext);
	}
}
