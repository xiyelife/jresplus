package com.hundsun.jresplus.web.velocity.resolver;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.springframework.web.servlet.view.velocity.VelocityToolboxView;

import com.hundsun.jresplus.web.contain.Contain;
import com.hundsun.jresplus.web.contain.async.AsynchronousContain;

/**
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class FixedVelocityLayoutView extends VelocityToolboxView {

	public static final String Seq = "/";

	public static final String DefaultLayoutPrefix = "layout/";

	public static final String DefaultLayoutName = "default.vm";

	public static final String DEFAULT_LAYOUT_URL = DefaultLayoutPrefix
			+ DefaultLayoutName;

	public static final String DEFAULT_LAYOUT_KEY = "layout";

	public static final String DEFAULT_SCREEN_CONTENT_KEY = "screen_content";

	private static final ThreadLocal<CharArrayWriterWrapper> screenLocal = new ThreadLocal<CharArrayWriterWrapper>() {

		@Override
		protected CharArrayWriterWrapper initialValue() {
			return new CharArrayWriterWrapper();
		}
	};

	private String layoutUrl = DEFAULT_LAYOUT_URL;

	private String layoutKey = DEFAULT_LAYOUT_KEY;

	private String screenContentKey = DEFAULT_SCREEN_CONTENT_KEY;

	private String layoutPrefix = DEFAULT_LAYOUT_KEY;
	private char[] layoutPrefixChars = layoutPrefix.toCharArray();

	private String defaultLayoutName = DefaultLayoutName;
	private char[] defaultLayoutNameChars = defaultLayoutName.toCharArray();

	private char[] screenPrefixChars;

	protected Map<Object, Template> layoutTemplateCache;

	public void setScreenPrefix(String prefix) {
		this.screenPrefixChars = prefix.toCharArray();
	}

	public void setLayoutUrl(String layoutUrl) {
		this.layoutUrl = layoutUrl;
		this.layoutPrefix = layoutUrl.substring(0, layoutUrl.indexOf(Seq) + 1);
		this.layoutPrefixChars = this.layoutPrefix.toCharArray();
		this.defaultLayoutName = layoutUrl
				.substring(layoutUrl.indexOf(Seq) + 1);
		this.defaultLayoutNameChars = this.defaultLayoutName.toCharArray();
	}

	public void setLayoutKey(String layoutKey) {
		this.layoutKey = layoutKey;
	}

	public void setScreenContentKey(String screenContentKey) {
		this.screenContentKey = screenContentKey;
	}

	@Override
	public boolean checkResource(Locale locale) throws Exception {
		return checkResource(getUrl());
	}

	protected boolean checkResource(String url) throws Exception {
		return this.getVelocityEngine().resourceExists(url);
	}

	private boolean isInContain(Context context) {
		Integer cCount = (Integer) context.get(Contain.ContainCounterKey);
		if (cCount != null && cCount > 0) {
			return true;
		}

		return false;
	}

	private boolean isInAsyncContain() {
		return AsynchronousContain.isAsyncConext();
	}

	@Override
	protected void doRender(Context context, HttpServletResponse response)
			throws Exception {
		if (isInAsyncContain()) {
			if (logger.isDebugEnabled()) {
				logger.debug("in async contain render for:" + this.getUrl());
			}
			renderCotainContent(context, response);
			return;
		}
		if (isInContain(context)) {
			if (logger.isDebugEnabled()) {
				logger.debug("in contain render for:" + this.getUrl());
			}
			renderCotainContent(context, response);
			return;
		}
		renderScreenContent(context);
		try {
			String layoutUrlToUse = (String) context.get(this.layoutKey);
			if (StringUtils.isNotBlank(layoutUrlToUse)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Screen content template has requested layout ["
							+ layoutUrlToUse + "]");
				}
				mergeTemplate(getTemplate(layoutUrlToUse), context, response);
			} else {
				if (layoutUrlToUse == null) {
					mergeTemplate(findLayoutTemplate(), context, response);
				} else {
					screenLocal.get().writeTo(response.getWriter());
				}
			}
		} finally {
			screenLocal.get().reset();
		}
	}

	protected Template findLayoutTemplate() throws Exception {
		Template cached = this.layoutTemplateCache.get(this.getUrl());
		if (cached == null) {
			cached = this.searchLayoutTemplate();
			this.layoutTemplateCache.put(this.getUrl(), cached);
		}
		return cached;
	}

	protected final Template searchLayoutTemplate() throws Exception {
		boolean debug = logger.isDebugEnabled();
		LayoutFinder finder = new LayoutFinder(this.getUrl(),
				this.layoutPrefixChars, this.screenPrefixChars,
				this.defaultLayoutNameChars);
		String url = finder.getSameNameLayoutUrl();
		if (checkResource(url)) {
			if (debug) {
				logger.debug("Find layout template [" + url + "] for:"
						+ getUrl());
			}
			return this.getTemplate(url);
		}
		while ((url = finder.getLayoutUrl()) != null) {
			if (checkResource(url)) {
				if (debug) {
					logger.debug("Find layout template [" + url + "] for:"
							+ getUrl());
				}
				return this.getTemplate(url);
			}
		}
		return this.getTemplate(this.layoutUrl);
	}

	private void renderScreenContent(Context velocityContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering screen content template [" + getUrl() + "]");
		}
		CharArrayWriterWrapper writer = screenLocal.get();
		Template screenContentTemplate = getTemplate(getUrl());
		screenContentTemplate.merge(velocityContext, writer.getWriter());
		velocityContext.put(this.screenContentKey, writer);
	}

	private void renderCotainContent(Context velocityContext,
			HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering contain content template [" + getUrl()
					+ "]");
		}
		Template cotainContentTemplate = getTemplate(getUrl());
		cotainContentTemplate.merge(velocityContext, response.getWriter());
	}

	@Override
	protected void mergeTemplate(Template template, Context context,
			HttpServletResponse response) throws Exception {
		super.mergeTemplate(template, context, response);
	}

	@Override
	protected boolean isCacheTemplate() {
		return false;
	}

	public void setLayoutTemplateCache(Map<Object, Template> layoutTemplateCache) {
		this.layoutTemplateCache = layoutTemplateCache;
	}
}
