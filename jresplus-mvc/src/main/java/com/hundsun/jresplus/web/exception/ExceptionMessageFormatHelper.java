package com.hundsun.jresplus.web.exception;

import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;

import com.hundsun.jresplus.exception.BaseException;

/**
 * <p>
 * </p>
 * 
 * @author: hanyin
 * @since: 13 Feb 2014 10:44:37
 * @history:
 ************************************************ 
 * @file: ExceptionMessageFormatHelper.java
 * @Copyright: 2013 恒生电子股份有限公司. All right reserved.
 ************************************************/
public class ExceptionMessageFormatHelper implements ExceptionFormater,
		InitializingBean {

	private MessageSource messageSource;

	/**
	 * 格式化异常信息
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public String format(Exception ex, Locale locale) {
		if (ex instanceof BaseException) {
			BaseException bex = (BaseException) ex;
			Object[] params = bex.getErrorParams();
			String message = messageSource.getMessage(bex.getErrorCode(),
					params, bex.getMessage(), locale);
			return message;
		}
		return ex.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(messageSource, "MessageSource 不能为空");
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
