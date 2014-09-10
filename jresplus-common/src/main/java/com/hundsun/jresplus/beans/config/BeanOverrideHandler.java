package com.hundsun.jresplus.beans.config;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * 功能描述: 抽取bean复写的逻辑到此类中<br>
 */
public class BeanOverrideHandler {
	private final static Logger logger = LoggerFactory
			.getLogger(BeanOverrideHandler.class);
	Set<String> bOverriderNames;
	Set<String> bOverridedNames;
	ConfigurableListableBeanFactory beanFactory;

	public BeanOverrideHandler(ConfigurableListableBeanFactory beanFactory) {
		super();
		this.beanFactory = beanFactory;
		bOverriderNames = new HashSet<String>();
		bOverridedNames = new HashSet<String>();
	}

	public void beanOverride() {
		beansOverride();
		removeOverrideBeanDef();
	}

	private void beansOverride() {
		String[] names = beanFactory.getBeanDefinitionNames();
		for (String beanName : names) {
			beanOverride(beanName);
		}
	}

	private void beanOverride(String beanName) {
		if (bOverridedNames.contains(beanName)) {
			return;
		}
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		String beanClassName = beanDefinition.getBeanClassName();
		if (StringUtil.isBlank(beanClassName)) {
			return;
		}
		try {
			Class<?> cls = Class.forName(beanClassName);
			if (hasOverrideAnnotation(cls)) {
				bOverriderNames.add(beanName);
				checkOverrideClass(cls);
				beanDefinitionOverride(beanClassName, cls);
			}
		} catch (Exception e) {
			logger.warn("OverrideBeanFactory bean definition process error bean["
					+ beanDefinition.getBeanClassName() + "]," + e);
		}
	}

	private boolean hasOverrideAnnotation(Class<?> cls) {
		return cls.isAnnotationPresent(BeanOverride.class) == true;
	}

	private void checkOverrideClass(Class<?> cls) {
		if (isOverrideSuperClass(cls)) {
			return;
		}
		if (interfaceNotMatched(cls)) {
			logger.error("initializing sagahl horn conext server falied step OverrideBean: the class and override must implements the same interface! ");
			throw new RuntimeException(
					"initializing sagahl horn conext server falied step OverrideBean: the class and override must implements the same interface! ");
		}
	}

	private boolean isOverrideSuperClass(Class<?> cls) {
		return cls.getSuperclass() == cls.getAnnotation(BeanOverride.class)
				.override();
	}

	private boolean interfaceNotMatched(Class<?> cls) {
		Class<?>[] overrideInterfaces = cls.getAnnotation(BeanOverride.class)
				.override().getInterfaces();
		for (Class<?> overrideClsInterface : overrideInterfaces) {
			if (hasInterface(cls, overrideClsInterface)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasInterface(Class<?> cls, Class<?> overrideClsInterface) {
		for (Class<?> clsInterface : cls.getInterfaces()) {
			if (overrideClsInterface == clsInterface) {
				return true;
			}
		}
		return false;
	}

	private void beanDefinitionOverride(String className, Class<?> cls) {
		Class<?> overrideCls = cls.getAnnotation(BeanOverride.class).override();
		for (String beanName : getMatchedBean(overrideCls)) {
			setBeanClass(beanFactory, className, beanName);
		}
	}

	private void setBeanClass(ConfigurableListableBeanFactory beanFactory,
			String className, String beanName) {
		BeanDefinition beanDefinitionTemp = beanFactory
				.getBeanDefinition(beanName);
		beanDefinitionTemp.setBeanClassName(className);
		bOverridedNames.add(beanName);
	}

	private Set<String> getMatchedBean(Class<?> overrideCls) {
		String[] namesTemps = beanFactory.getBeanDefinitionNames();
		Set<String> matchedBean = new HashSet<String>();
		for (String nameTemp : namesTemps) {
			BeanDefinition beanDefinitionTemp = beanFactory
					.getBeanDefinition(nameTemp);
			if (overrideCls.getName().equals(
					beanDefinitionTemp.getBeanClassName())) {
				matchedBean.add(nameTemp);
			}
		}
		return matchedBean;
	}

	private void removeOverrideBeanDef() {
		for (String nameTemp : bOverriderNames) {
			if (beanFactory instanceof BeanDefinitionRegistry) {
				BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
				beanDefinitionRegistry.removeBeanDefinition(nameTemp);
			}
		}
	}
}
