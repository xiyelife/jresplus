package com.hundsun.jresplus.web.url;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class URLBrokerLauncherFilter extends OncePerRequestFilter {

	private Map<String, URLBroker> brokers;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		for (Entry<String, URLBroker> entry : brokers.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}
		filterChain.doFilter(request, response);
	}

	public Map<String, URLBroker> getBrokers() {
		return brokers;
	}

	public void setBrokers(Map<String, URLBroker> brokers) {
		this.brokers = brokers;
	}

}
