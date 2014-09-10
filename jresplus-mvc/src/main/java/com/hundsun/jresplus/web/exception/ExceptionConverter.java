package com.hundsun.jresplus.web.exception;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
/**
 * 异常转换器，可将异常对象转换为其他可序列化的对象用于视图输出
 * @author XIE (xjj@hundsun.com)
 *
 */
public interface ExceptionConverter {
	public Serializable convert(HttpServletRequest request, Exception ex);
}
