package com.hundsun.jresplus.web.contain;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.filter.GenericFilterBean;

import com.hundsun.jresplus.web.contain.async.AsynchronousContain;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class ContainFilter extends GenericFilterBean implements Filter,
		ServletContextAware {

	private String containKey = "contain";

	private String asyncContainKey = "asyncContain";

	private boolean onOff = true;

	public void setOnOff(boolean onOff) {
		this.onOff = onOff;
	}

	public String getContainKey() {
		return containKey;
	}

	public void setContainKey(String containKey) {
		this.containKey = containKey;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request.getAttribute(containKey) != null) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		AsynchronousContain asyncContain = null;
		if (onOff) {
			asyncContain = new AsynchronousContain(httpRequest, httpResponse);
			request.setAttribute(asyncContainKey, asyncContain);
		}
		request.setAttribute(containKey, new Contain(httpRequest, httpResponse));
		try {
			chain.doFilter(httpRequest, httpResponse);
		} finally {
			if (onOff && asyncContain != null) {
				asyncContain.finished();
				request.removeAttribute(asyncContainKey);
			}
			request.removeAttribute(containKey);
		}
	}
}
