/*
 * 修改记录
 * 2014-2-7		STORY #7563 -nosession的cookiestore支持domain
 */
package com.hundsun.jresplus.web.nosession.cookie;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
public class Cookie extends javax.servlet.http.Cookie implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7271090104478428146L;

	private boolean httpOnly = false;

	public Cookie(String name, String value) {
		super(name, value);
	}

	public Cookie(String name, String value, boolean httpOnly) {
		super(name, value);
		this.httpOnly = httpOnly;
	}

	public Cookie(String name, String value, boolean httpOnly, int maxAge) {
		super(name, value);
		setMaxAge(maxAge);
		this.httpOnly = httpOnly;
	}

	public Cookie(String name, String value, boolean httpOnly, int maxAge,
			String path) {
		super(name, value);
		setMaxAge(maxAge);
		setPath(path);
		this.httpOnly = httpOnly;
	}

	public Cookie(String name, String value, boolean httpOnly, int maxAge,
			String path, String domain) {
		super(name, value);
		setMaxAge(maxAge);
		setPath(path);
		if (StringUtil.isEmpty(domain) == false) {
			setDomain(domain);
		}
		this.httpOnly = httpOnly;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	public Object clone() {
		return super.clone();
	}
}
