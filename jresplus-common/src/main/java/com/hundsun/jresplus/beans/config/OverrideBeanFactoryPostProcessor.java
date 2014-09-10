package com.hundsun.jresplus.beans.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * @author LeoHu copy by sagahl
 */
public class OverrideBeanFactoryPostProcessor implements
		BeanFactoryPostProcessor, PriorityOrdered {

	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		new BeanOverrideHandler(beanFactory).beanOverride();
	}

}
