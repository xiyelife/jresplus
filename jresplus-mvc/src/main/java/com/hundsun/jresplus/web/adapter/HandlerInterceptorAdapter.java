package com.hundsun.jresplus.web.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;

import com.hundsun.jresplus.beans.ObjectFactory;
import com.hundsun.jresplus.web.contain.Contain;
import com.hundsun.jresplus.web.interceptors.HandlerInterceptor;
import com.hundsun.jresplus.web.servlet.MediaTypesHandler;

/**
 * 
 * @author LeoHu copy sagahl
 * 
 */
public class HandlerInterceptorAdapter extends
		org.springframework.web.servlet.handler.HandlerInterceptorAdapter {

	private MediaTypesHandler mediaTypesHandler;

	@Value("${response.out.charset}")
	private String outCharset;

	public void setMediaTypesHandler(MediaTypesHandler mediaTypesHandler) {
		this.mediaTypesHandler = mediaTypesHandler;
	}

	private List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.interceptors = objectFactory
				.getBeansOfType4List(HandlerInterceptor.class);
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if (Contain.isInContain(request) == false) {
			List<MediaType> list = mediaTypesHandler.getMediaTypes(request);
			response.setContentType(list.get(0).toString() + ";charset="
					+ outCharset);
		}
		if (interceptors != null) {
			for (HandlerInterceptor interceptor : interceptors) {
				if (interceptor.preHandle(request, response, handler) == false) {
					return false;
				}
			}
		}
		return super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (interceptors != null) {
			for (HandlerInterceptor interceptor : interceptors) {
				interceptor
						.postHandle(request, response, handler, modelAndView);
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (interceptors != null) {
			for (HandlerInterceptor interceptor : interceptors) {
				interceptor.afterCompletion(request, response, handler, ex);
			}
		}
	}

}
