package com.hundsun.jresplus.beans.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class PropertyLocationListener implements ServletContextListener {
	public static String CONFIG_LOCATION = "JresConfigLocation";

	public void contextInitialized(ServletContextEvent event) {
		String location = event.getServletContext().getInitParameter(
				CONFIG_LOCATION);
		System.setProperty(CONFIG_LOCATION, location);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		System.clearProperty(CONFIG_LOCATION);
	}

}
