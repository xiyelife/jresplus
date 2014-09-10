package com.hundsun.jresplus.web.cache;

import java.io.Serializable;

import javax.servlet.http.Cookie;

public class PageCookie implements Serializable {
	private static final long serialVersionUID = 8628587700329421486L;
	private String name;
	private String value;
	private String comment;
	private String domain;
	private int maxAge;
	private String path;
	private boolean secure;
	private int version;
	private boolean httpOnly = false;
	private boolean isNosession = false;

	public PageCookie(Cookie cookie) {
		this.name = cookie.getName();
		this.value = cookie.getValue();
		this.comment = cookie.getComment();
		this.domain = cookie.getDomain();
		this.maxAge = cookie.getMaxAge();
		this.path = cookie.getPath();
		this.secure = cookie.getSecure();
		this.version = cookie.getVersion();
		if (cookie instanceof com.hundsun.jresplus.web.nosession.cookie.Cookie) {
			com.hundsun.jresplus.web.nosession.cookie.Cookie nosessionCookie = (com.hundsun.jresplus.web.nosession.cookie.Cookie) cookie;
			httpOnly = nosessionCookie.isHttpOnly();
			isNosession = true;
		}
	}

	public Cookie toCookie() {
		Cookie cookie;
		if (isNosession) {
			com.hundsun.jresplus.web.nosession.cookie.Cookie nosessionCookie = new com.hundsun.jresplus.web.nosession.cookie.Cookie(
					this.name, this.value);
			nosessionCookie.setHttpOnly(httpOnly);
			cookie = nosessionCookie;
		} else {
			cookie = new Cookie(this.name, this.value);
		}

		cookie.setComment(this.comment);

		if (this.domain != null) {
			cookie.setDomain(this.domain);
		}
		cookie.setMaxAge(this.maxAge);
		cookie.setPath(this.path);
		cookie.setSecure(this.secure);
		cookie.setVersion(this.version);
		return cookie;
	}
}
