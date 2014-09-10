/*
 * 修改记录
 * 2013-8-13  STORY #6533 增加过滤器链机制支持减少配置项
 */
package com.hundsun.jresplus.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.filter.GenericFilterBean;

/**
 * 
 * @author huling copy by sagahl copy by fish
 * 
 */
public class SimpleOncePerRequestFilterChain extends GenericFilterBean {

	private final String AlreadFilteredTag = SimpleOncePerRequestFilterChain.class
			.getSimpleName() + ".FILTERED";

	private Filter[] filters = new Filter[0];

	public void setFilters(List<Filter> fs) {
		if (fs == null || fs.isEmpty()) {
			return;
		}
		filters = fs.toArray(new Filter[fs.size()]);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Object obj = request.getAttribute(AlreadFilteredTag);
		if (obj != null) {
			// alread filterd
			chain.doFilter(request, response);
			return;
		}
		request.setAttribute(AlreadFilteredTag, Boolean.TRUE);
		try {
			SimpleFilterChain simpleChain = new SimpleFilterChain(filters,
					chain);
			simpleChain.doFilter(request, response);
		} finally {
			request.removeAttribute(AlreadFilteredTag);
		}
	}

	protected static final class SimpleFilterChain implements FilterChain {

		private Filter[] fs;

		private FilterChain chain;

		private int currentFilter = -1;

		private int filtersSize;

		public SimpleFilterChain(Filter[] fs, FilterChain chain) {
			super();
			this.fs = fs;
			this.chain = chain;
			this.filtersSize = fs.length;
		}

		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException {
			currentFilter++;
			if (currentFilter == filtersSize) {
				// end
				chain.doFilter(request, response);
			} else {
				fs[currentFilter].doFilter(request, response, this);
			}
		}
	}

}
