package com.hundsun.jresplus.web.adapter;

import java.lang.reflect.Method;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author LeoHu copy by sagahl
 * 
 */
public interface ReturnValueHandlerResolver {

	Object resolveReturnValue(Method handlerMethod, Class<?> handlerType,
			Object returnValue, ExtendedModelMap implicitModel,
			NativeWebRequest webRequest);
}
