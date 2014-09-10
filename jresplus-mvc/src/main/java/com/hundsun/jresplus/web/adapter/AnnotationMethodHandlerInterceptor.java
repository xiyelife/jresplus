package com.hundsun.jresplus.web.adapter;

import java.lang.reflect.Method;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author LeoHu copy by sagahl copy by fish
 * @version $Id: AnnotationMethodHandlerInterceptor.java,v 1.1 2011/06/20 08:11:11 fish Exp $
 */
public interface AnnotationMethodHandlerInterceptor {
	public void preInvoke(Method handlerMethod, Object handler,
			ServletWebRequest webRequest);

	public void postInvoke(Method handlerMethod, Object handler,
			ServletWebRequest webRequest, ModelAndView mav);
}
