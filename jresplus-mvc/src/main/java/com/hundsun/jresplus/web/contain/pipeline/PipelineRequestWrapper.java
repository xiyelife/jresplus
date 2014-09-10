package com.hundsun.jresplus.web.contain.pipeline;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.springframework.web.util.WebUtils;

public class PipelineRequestWrapper extends HttpServletRequestWrapper {

	private static final Set<String> exclude = new HashSet<String>();
	static {
		exclude.add(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		exclude.add(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
		exclude.add(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
		exclude.add(WebUtils.INCLUDE_PATH_INFO_ATTRIBUTE);
		exclude.add(WebUtils.INCLUDE_QUERY_STRING_ATTRIBUTE);
		exclude.add(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
		exclude.add(WebUtils.FORWARD_CONTEXT_PATH_ATTRIBUTE);
		exclude.add(WebUtils.FORWARD_SERVLET_PATH_ATTRIBUTE);
		exclude.add(WebUtils.FORWARD_PATH_INFO_ATTRIBUTE);
		exclude.add(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
	}

	private Map<String, Object> parameters;

	private String contextPath, method, requestURI, encoding;

	public PipelineRequestWrapper(HttpServletRequest request,
			Map<String, Object> parameters, String uri) {
		super(request);
		this.contextPath = request.getContextPath();
		this.method = request.getMethod();
		this.requestURI = uri;
		this.encoding = request.getCharacterEncoding();
		this.parameters = new HashMap<String, Object>();
		for (Enumeration<String> en = request.getAttributeNames(); en
				.hasMoreElements();) {
			String name = en.nextElement();
			if (!exclude.contains(name)) {
				copyAttrbiute(name);
			}
		}
		this.parameters.putAll(parameters);
	}

	private final void copyAttrbiute(String name) {
		this.parameters.put(name, super.getAttribute(name));
	}

	@Override
	public void setAttribute(String name, Object o) {
		this.parameters.put(name, o);
	}

	@Override
	public Object getAttribute(String name) {
		if (this.parameters.containsKey(name)) {
			return this.parameters.get(name);
		}
		return super.getAttribute(name);
	}

	@Override
	public void removeAttribute(String name) {
		this.parameters.remove(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration(this.parameters.keySet().iterator());
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public String getServletPath() {
		return requestURI;
	}

	@Override
	public String getCharacterEncoding() {
		return encoding;
	}

	@Override
	public String getPathInfo() {
		return requestURI;
	}

}
