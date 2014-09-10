/*
 * 修改记录
 * 2014-2-7		STORY #7563 -nosession的cookiestore支持domain
 */
package com.hundsun.jresplus.web.nosession.cookie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hundsun.jresplus.common.util.ArrayUtil;
import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.web.nosession.SessionEncoderException;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
class CookiesManagerImpl implements CookiesManager {
	private final static Logger log = LoggerFactory
			.getLogger(CookiesManagerImpl.class);
	private static final String COOKIE_NAME_PATTERN_SUFFIX = "(\\d+)";
	private int maxLength, maxCount;

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	private static class CookieInfo implements Comparable<CookieInfo> {

		public final int index;
		public final String value;

		public CookieInfo(int index, String value) {
			this.index = index;
			this.value = value;
		}

		public int compareTo(CookieInfo o) {
			return index - o.index;
		}
	}

	private String mergeCookies(String cookieName, List<CookieInfo> cookieList) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cookieList.size(); i++) {
			CookieInfo info = null;
			for (CookieInfo cookieInfo : cookieList) {
				if (cookieInfo.index == i) {
					info = cookieInfo;
					break;
				}
			}
			if (info == null) {
				log.warn("the cookie index error,the cookieName = "
						+ cookieName + ".the lost array No is:" + i);
				return null;
			} else {
				sb.append(info.value);
			}
		}
		return sb.toString();
	}

	public String readCookieValue(HttpServletRequest servletRequest,
			String cookieName) {
		javax.servlet.http.Cookie[] cookies = servletRequest.getCookies();
		List<CookieInfo> cookieList = new ArrayList<CookieInfo>();
		Pattern namePattern = Pattern.compile(cookieName
				+ COOKIE_NAME_PATTERN_SUFFIX);
		if (cookies == null) {
			return null;
		}
		for (javax.servlet.http.Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
			Matcher matcher = namePattern.matcher(cookie.getName());
			if (matcher.matches()) {
				int index = Integer.parseInt(matcher.group(1));
				CookieInfo cookieInfo = new CookieInfo(index,
						StringUtil.trimToNull(cookie.getValue()));
				cookieList.add(cookieInfo);
			}
		}
		Collections.sort(cookieList);
		return mergeCookies(cookieName, cookieList);
	}

	private void clearGarbage(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, String cookieName,
			int startNum, String path, String domain) {
		Pattern namePattern = Pattern.compile(cookieName
				+ COOKIE_NAME_PATTERN_SUFFIX);
		javax.servlet.http.Cookie[] cookies = servletRequest.getCookies();
		if (ArrayUtil.isEmpty(cookies)) {
			return;
		}
		for (javax.servlet.http.Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName()) && startNum != 0) {
				cookie.setMaxAge(0);
				cookie.setValue(null);
				cookie.setPath(path);
				if (StringUtil.isEmpty(domain) == false) {
					cookie.setDomain(domain);
				}
				servletResponse.addCookie(cookie);
			}
			Matcher matcher = namePattern.matcher(cookie.getName());
			if (matcher.matches()) {
				int index = Integer.parseInt(matcher.group(1));
				if (index > startNum) {
					cookie.setMaxAge(0);
					cookie.setValue(null);
					cookie.setPath(path);
					if (StringUtil.isEmpty(domain) == false) {
						cookie.setDomain(domain);
					}
					servletResponse.addCookie(cookie);
				}
			}
		}
	}

	public void writeCookie(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse,
			com.hundsun.jresplus.web.nosession.cookie.Cookie cookie)
			throws SessionEncoderException {
		int startNum = 0;
		if (cookie.getValue() != null) {
			if (cookie.getValue().length() > maxLength * maxCount) {
				log.error("Cookie store full! All session attributes will be LOST! the cookies length="
						+ cookie.getValue().length()
						+ ", and the maxlength="
						+ maxLength);
				throw new SessionEncoderException(
						"Cookie store full! All session attributes will be LOST!");
			}

			if (cookie.getValue().length() + cookie.getName().length() > maxLength) {
				for (int beginOffset = 0, i = 1; beginOffset < cookie
						.getValue().length(); beginOffset += (maxLength - cookie
						.getName().length()), i++) {
					int endOffset = Math.min(beginOffset
							+ (maxLength - cookie.getName().length()), cookie
							.getValue().length());
					com.hundsun.jresplus.web.nosession.cookie.Cookie harpCookie = new com.hundsun.jresplus.web.nosession.cookie.Cookie(
							cookie.getName() + startNum, cookie.getValue()
									.substring(beginOffset, endOffset),
							cookie.isHttpOnly(), cookie.getMaxAge(),
							cookie.getPath(), cookie.getDomain());
					servletResponse.addCookie(harpCookie);
					startNum = i;
				}
			} else {
				servletResponse.addCookie(cookie);
			}
		} else {
			startNum = -1;
		}
		clearGarbage(servletRequest, servletResponse, cookie.getName(),
				startNum, cookie.getPath(), cookie.getDomain());
	}
}
