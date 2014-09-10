package com.hundsun.jresplus.web.exception;

import java.util.Locale;

/** 
 * <p></p>
 * @author: hanyin
 * @since: 13 Feb 2014  10:38:37
 * @history:
 ************************************************
 * @file: ExceptionFormater.java
 * @Copyright: 2013 恒生电子股份有限公司.
 * All right reserved.
 ************************************************/

public interface ExceptionFormater {
	String format(Exception ex, Locale locale);
}
