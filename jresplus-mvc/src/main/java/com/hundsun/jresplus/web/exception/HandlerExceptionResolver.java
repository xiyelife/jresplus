package com.hundsun.jresplus.web.exception;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.support.RequestContext;

import com.hundsun.jresplus.beans.ObjectFactory;
import com.hundsun.jresplus.exception.BaseException;
import com.hundsun.jresplus.web.servlet.MediaTypesHandler;

/**
 * <p>
 * 拦截BaseException和其他非jresplus异常
 * </p>
 * 
 * @author: hanyin
 * @since: 12 Feb 2014 13:32:23
 * @history:
 ************************************************ 
 * @file: HandlerExceptionResolver.java
 * @Copyright: 2013 恒生电子股份有限公司. All right reserved.
 ************************************************/

public class HandlerExceptionResolver extends SimpleMappingExceptionResolver
		implements InitializingBean {
	private final static Logger logger = LoggerFactory
			.getLogger(HandlerExceptionResolver.class);

	/** 错误处理页面的路径 */
	private String errorPath = "/500";

	/** json、xml等处理模板的路径 */
	@Deprecated
	private String exceptionProcPath = "contain/exception";
	/** json、xml等处理模板的路径 */
	private String defaultAjaxErrorView = "contain/exception";
	private ExceptionFormater formatHelper;

	@Value("${system.dev.mode}")
	private boolean flag = false;

	@Autowired
	private MediaTypesHandler mediaTypesHandler;
	@Autowired
	private ObjectFactory objectFactory;
	private MessageSource messageSource;

	private ExceptionConverter exceptionConverter;

	public HandlerExceptionResolver() {
		super();
		super.setDefaultErrorView(errorPath);
	}

	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		RequestContext requestContext = new RequestContext(request);
		String format = formatHelper.format(ex, requestContext.getLocale());

		if (ex instanceof BaseException) {
			((BaseException) ex).setErrorMessage(format);
		} else {
			logger.error("未知业务异常[" + format + "]", ex);
		}

		if (flag) {
			/** 如果是开发模式，直接在页面上返回 */
			return null;
		} else {
			if (mediaTypesHandler.getMediaTypes(request).get(0)
					.equals(MediaType.TEXT_HTML)) {
				return super.doResolveException(request, response, handler, ex);
			}
			if (mediaTypesHandler.getMediaTypes(request).get(0)
					.equals(MediaType.APPLICATION_JSON)) {
				request.setAttribute("mediaType", "JSON");
			} else if (mediaTypesHandler.getMediaTypes(request).get(0)
					.equals(MediaType.APPLICATION_XML)) {
				request.setAttribute("mediaType", "XML");
			}

			return getModelAndView(defaultAjaxErrorView, ex, request);
		}
	}

	protected ModelAndView getModelAndView(String viewName, Exception ex,
			HttpServletRequest request) {

		request.setAttribute("layout", "");
		if (exceptionConverter == null) {
			return super.getModelAndView(viewName, ex, request);
		}
		Serializable obj = exceptionConverter.convert(request, ex);
		ModelAndView mv = new ModelAndView(viewName);
		if (obj != null) {
			mv.addObject(DEFAULT_EXCEPTION_ATTRIBUTE, obj);
		}
		return mv;
	}

	public String getErrorPath() {
		return errorPath;
	}

	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}

	@Deprecated
	public String getExceptionProcPath() {
		return exceptionProcPath;
	}

	@Deprecated
	public void setExceptionProcPath(String exceptionProcPath) {
		this.exceptionProcPath = exceptionProcPath;
		this.defaultAjaxErrorView = exceptionProcPath;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public ExceptionFormater getFormatHelper() {
		return formatHelper;
	}

	public void setFormatHelper(ExceptionFormater formatHelper) {
		this.formatHelper = formatHelper;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setExceptionConverter(ExceptionConverter exceptionConverter) {
		this.exceptionConverter = exceptionConverter;
	}

	public String getDefaultAjaxErrorView() {
		return defaultAjaxErrorView;
	}

	public void setDefaultAjaxErrorView(String defaultAjaxErrorView) {
		this.defaultAjaxErrorView = defaultAjaxErrorView;
	}

	public void afterPropertiesSet() throws Exception {
		logger.info("Use Exception resolver.");
		if (formatHelper != null) {
			return;
		}
		if (messageSource == null) {
			messageSource = objectFactory.getBean(MessageSource.class);
		}
		Assert.notNull(messageSource, "MessageSource 不能为空");
		ExceptionMessageFormatHelper format = new ExceptionMessageFormatHelper();
		format.setMessageSource(messageSource);
		formatHelper = format;
	}
}
