package com.hundsun.jresplus.web.velocity;

import javax.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

/**
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class FixedVelocityConfigurer extends VelocityConfigurer {

	private ServletContext servletContext;

	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
		this.servletContext = servletContext;
	}

	@Override
	protected void postProcessVelocityEngine(VelocityEngine velocityEngine) {
		velocityEngine.setApplicationAttribute(ServletContext.class.getName(),
				this.servletContext);
	}

}
