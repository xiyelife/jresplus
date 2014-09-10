package com.hundsun.jresplus.web.url;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.hundsun.jresplus.beans.ObjectFactory;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class ServletContextInitHelper extends URLBrokerInitHelper implements
		ServletContextAware, InitializingBean {

	private ServletContext servletContext;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Autowired
	ObjectFactory objectFactory;

	public void afterPropertiesSet() throws Exception {
		try {
			Method getContextPath = ServletContext.class.getDeclaredMethod(
					"getContextPath", new Class[] {});
			if (getContextPath != null) {
				String contextPath = (String) getContextPath.invoke(
						servletContext, new Object[] {});
				if (logger.isDebugEnabled()) {
					logger.debug("Servlet 2.5 implement find.so init context path:"
							+ contextPath);
				}
				List<URLBroker> regBroker = objectFactory
						.getBeansOfType4List(URLBroker.class);
				if (regBroker == null) {
					return;
				}
				for (URLBroker broker : regBroker) {
					broker.init(contextPath);
					logger.info("URL Broker[{}-{}] inited.", broker.getName(),
							broker.getClass());
				}
			}
		} catch (Exception e) {
			logger.error("error in init servlet context path", e);
		}

	}

}
