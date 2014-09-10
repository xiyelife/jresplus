package com.hundsun.jresplus.web.url;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.hundsun.jresplus.beans.ObjectFactory;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class URLBrokerLauncherInterceptor extends HandlerInterceptorAdapter
		implements InitializingBean {
	private static String myTag = "_"
			+ URLBrokerLauncherInterceptor.class.getName();

	private static String tagValue = "1";
	@Autowired
	private ObjectFactory objectFactory;

	private Map<String, URLBroker> brokers;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		Object tag = request.getAttribute(myTag);
		if (tag == null) {
			for (Entry<String, URLBroker> entry : brokers.entrySet()) {
				request.setAttribute(entry.getKey(), entry.getValue());
			}
			request.setAttribute(myTag, tagValue);
		}

		return super.preHandle(request, response, handler);
	}

	public Map<String, URLBroker> getBrokers() {
		return brokers;
	}

	public void setBrokers(Map<String, URLBroker> brokers) {
		this.brokers = brokers;
	}

	public void afterPropertiesSet() throws Exception {
		List<URLBroker> regBroker = objectFactory
				.getBeansOfType4List(URLBroker.class);
		if (regBroker == null) {
			return;
		}
		if (brokers == null) {
			brokers = new HashMap<String, URLBroker>();
		}
		for (URLBroker broker : regBroker) {
			if (broker.getName() == null) {
				continue;
			}
			if (brokers.containsKey(broker.getName())) {
				continue;
			}
			brokers.put(broker.getName(), broker);
		}
	}

}
