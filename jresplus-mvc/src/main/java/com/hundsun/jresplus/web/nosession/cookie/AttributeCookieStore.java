/*
 * 修改记录
 * 2013-9-29 增加cookie方式的session存储单元接口替换原来继承方式的扩展机制
 * 2014-2-7		STORY #7563 -nosession的cookiestore支持domain
 */
package com.hundsun.jresplus.web.nosession.cookie;

import java.util.Set;

import org.springframework.core.Ordered;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
public interface AttributeCookieStore extends Ordered {

	public boolean isMatch(String key);

	public String getCookieName();

	public Encode getEncode();

	public int getMaxInactiveInterval();

	public String getPath();

	public Set<String> getAttributeNames();

	public String getDomain();
}
