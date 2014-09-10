package com.hundsun.jresplus.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.hundsun.jresplus.beans.config.PropertyPlaceholderConfigurer;
import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.web.contain.Contain;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
public class DispatcherServlet extends
		org.springframework.web.servlet.DispatcherServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6596760152603760291L;

	public static final String ACTION_COMPONENT_SCAN = "jres.action.scan";

	public String getContextConfigLocation() {
		String config = super.getContextConfigLocation();
		if (StringUtil.isEmpty(config)) {
			return "classpath:conf/spring/web/jres-web-main.xml";
		} else {
			return "classpath:conf/spring/web/jres-web-main.xml," + config;
		}
	}

	/**
	 * Exposes the DispatcherServlet-specific request attributes and delegates
	 * to {@link #doDispatch} for the actual dispatching.
	 */
	protected void doService(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (Contain.isInContain(request)) {
			super.doDispatch(request, response);
		} else {
			super.doService(request, response);
		}
	}

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
	 * or a {@link #setContextClass custom context class}, if set.
	 * <p>
	 * This implementation expects custom contexts to implement the
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}
	 * interface. Can be overridden in subclasses.
	 * <p>
	 * Do not forget to register this servlet instance as application listener
	 * on the created context (for triggering its {@link #onRefresh callback},
	 * and to call
	 * {@link org.springframework.context.ConfigurableApplicationContext#refresh()}
	 * before returning the context instance.
	 * 
	 * @param parent
	 *            the parent ApplicationContext to use, or <code>null</code> if
	 *            none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(
			ApplicationContext parent) {
		PropertyPlaceholderConfigurer configurer = (PropertyPlaceholderConfigurer) parent
				.getBean("ResourceConfigurer");
		try {
			try {
				System.setProperty(ACTION_COMPONENT_SCAN, configurer
						.mergeProperties().getProperty(ACTION_COMPONENT_SCAN));
			} catch (IOException e) {
				throw new IllegalArgumentException(
						"Could not resolve placeholder '"
								+ ACTION_COMPONENT_SCAN + "'");
			}
			return super.createWebApplicationContext(parent);
		} finally {
			System.clearProperty(ACTION_COMPONENT_SCAN);
		}
	}
}
