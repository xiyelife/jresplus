package com.hundsun.jresplus.web.nosession.cookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hundsun.jresplus.web.nosession.SessionEncoderException;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
public interface CookiesManager {

	public String readCookieValue(HttpServletRequest servletRequest, String name);

	public void writeCookie(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, Cookie cookie)
			throws SessionEncoderException;
}
