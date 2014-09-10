/*
 * 2013-8-13	STORY #6535 [研发中心/内部需求][jresplus/mvc]validation扩展bean增加validationMessageSource的beanname注入支持
 */
package com.hundsun.jresplus.beans;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;

/**
 * 
 * @author LeoHu copy sagahl
 * 
 */
public class ObjectFactoryImpl implements ObjectFactory,
		ApplicationContextAware {

	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		this.autowireCapableBeanFactory = applicationContext
				.getAutowireCapableBeanFactory();
	}

	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanClass, boolean dependencyCheck) {
		return (T) autowireCapableBeanFactory.createBean(beanClass,
				AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, dependencyCheck);
	}

	public void autowireBeanProperties(Object existingBean) {
		autowireCapableBeanFactory.autowireBeanProperties(existingBean,
				AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);

	}

	public <T> Map<String, T> getBeansOfType4Map(Class<T> beanClass) {
		return applicationContext.getBeansOfType(beanClass);
	}

	public <T> List<T> getBeansOfType4List(Class<T> beanClass) {
		Map<String, T> map = getBeansOfType4Map(beanClass);
		if (map != null && map.size() > 0) {
			List<T> list = new ArrayList<T>(map.values());
			OrderComparator.sort(list);
			return list;
		}
		return null;
	}

	public Object getBean(String name) {
		try {
			return applicationContext.getBean(name);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}

	public <T> T getBean(Class<T> beanClass) {
		try {
			return applicationContext.getBean(beanClass);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}

	public <T> T[] getBeansOfType4Array(Class<T> beanClass) {
		Map<String, T> map = getBeansOfType4Map(beanClass);
		if (map != null && map.size() > 0) {
			@SuppressWarnings("unchecked")
			T[] result = (T[]) Array.newInstance(beanClass, map.size());
			int n = 0;
			for (Entry<String, T> entry : map.entrySet()) {
				result[n] = entry.getValue();
				n++;
			}
			OrderComparator.sort(result);
			return result;
		}
		return null;
	}
}