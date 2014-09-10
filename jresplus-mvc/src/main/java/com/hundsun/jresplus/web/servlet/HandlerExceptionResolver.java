package com.hundsun.jresplus.web.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.hundsun.jresplus.web.contain.Contain;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
public class HandlerExceptionResolver implements
		org.springframework.web.servlet.HandlerExceptionResolver {

	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, final Exception ex) {
		if (Contain.isInContain(request)) {
			return new ModelAndView(new View() {

				public void render(Map<String, ?> model,
						HttpServletRequest request, HttpServletResponse response)
						throws Exception {
					response.getWriter().write(ex.getMessage());
				}

				public String getContentType() {
					return "text/html";
				}
			});
		}
		return null;
	}
}
