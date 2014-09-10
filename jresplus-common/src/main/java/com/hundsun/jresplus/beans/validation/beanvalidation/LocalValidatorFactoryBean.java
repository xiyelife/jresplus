/*
 * 修改记录
 * 2013-8-13	STORY #6535 validation扩展bean增加validationMessageSource的beanname注入支持
 */
package com.hundsun.jresplus.beans.validation.beanvalidation;

import org.springframework.context.MessageSource;

import com.hundsun.jresplus.beans.ObjectFactory;
import com.hundsun.jresplus.common.util.StringUtil;

public class LocalValidatorFactoryBean extends
		org.springframework.validation.beanvalidation.LocalValidatorFactoryBean {

	private ObjectFactory objectFactory;

	private boolean webVaild = false;

	private String validationMessageBeanName;

	public void setValidationMessageBeanName(String validationMessageBeanName) {
		this.validationMessageBeanName = validationMessageBeanName;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public boolean isWebVaild() {
		return webVaild;
	}

	public void setWebVaild(boolean webVaild) {
		this.webVaild = webVaild;
	}

	@Override
	public void afterPropertiesSet() {
		if (isWebVaild()) {
			if (StringUtil.isEmpty(validationMessageBeanName) == false) {
				super.setValidationMessageSource((MessageSource) objectFactory
						.getBean(validationMessageBeanName));
			}
			super.afterPropertiesSet();
		}
	}
}
