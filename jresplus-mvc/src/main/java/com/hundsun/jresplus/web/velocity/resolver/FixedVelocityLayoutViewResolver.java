package com.hundsun.jresplus.web.velocity.resolver;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

/**
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class FixedVelocityLayoutViewResolver extends VelocityViewResolver {

	private String layoutUrl;

	private String layoutKey;

	private String screenContentKey;

	private String templateEncoding = "UTF-8";

	private VelocityEngine velocityEngine = null;

	private Map<Object, Template> layoutTemplateCache = EMPTY_MAP;

	public FixedVelocityLayoutViewResolver() {
		super();
		this.setExposeRequestAttributes(true);
	}

	public void setDevMode(boolean devMode) {
		if (devMode) {
			layoutTemplateCache = EMPTY_MAP;
		} else {
			layoutTemplateCache = new ConcurrentHashMap<Object, Template>();
		}
	}

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public String getTemplateEncoding() {
		return templateEncoding;
	}

	public void setTemplateEncoding(String templateEncoding) {
		this.templateEncoding = templateEncoding;
	}

	public void setLayoutUrl(String layoutUrl) {
		this.layoutUrl = layoutUrl;
	}

	public void setLayoutKey(String layoutKey) {
		this.layoutKey = layoutKey;
	}

	public void setScreenContentKey(String screenContentKey) {
		this.screenContentKey = screenContentKey;
	}

	@Override
	public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
		if (!exposeRequestAttributes) {
			throw new IllegalArgumentException(
					"exposeRequestAttributes must be true in FixedVelocityLayoutViewResolver.");
		}
	}

	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		FixedVelocityLayoutView view = (FixedVelocityLayoutView) super
				.buildView(viewName);
		view.setContentType(this.getContentType());
		view.setEncoding(this.templateEncoding);
		if (this.velocityEngine != null) {
			view.setVelocityEngine(this.velocityEngine);
		}
		if (this.layoutUrl != null) {
			view.setLayoutUrl(this.layoutUrl);
		}
		if (this.layoutKey != null) {
			view.setLayoutKey(this.layoutKey);
		}
		if (this.screenContentKey != null) {
			view.setScreenContentKey(this.screenContentKey);
		}
		view.setScreenPrefix(this.getPrefix());
		view.setLayoutTemplateCache(this.layoutTemplateCache);
		return view;
	}

	@Override
	protected Class<?> requiredViewClass() {
		return FixedVelocityLayoutView.class;
	}

	@Override
	protected Class<?> getViewClass() {
		return FixedVelocityLayoutView.class;
	}

	private static final Map<Object, Template> EMPTY_MAP = new EmptyMap();

	private static class EmptyMap extends AbstractMap<Object, Template>
			implements Serializable {

		private static final long serialVersionUID = 6502570486462028098L;

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public boolean containsKey(Object key) {
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			return false;
		}

		@Override
		public Template get(Object key) {
			return null;
		}

		@Override
		public Set<Object> keySet() {
			return Collections.<Object> emptySet();
		}

		@Override
		public Collection<Template> values() {
			return Collections.<Template> emptySet();
		}

		@Override
		public Set<Map.Entry<Object, Template>> entrySet() {
			return Collections.emptySet();
		}

		@Override
		public Template put(Object key, Template value) {
			return null;
		}

		@Override
		public Template remove(Object key) {
			return null;
		}

		@Override
		public void putAll(Map<? extends Object, ? extends Template> m) {
		}

		public boolean equals(Object o) {
			return (o instanceof Map) && ((Map) o).size() == 0;
		}

		public int hashCode() {
			return 1;
		}

		private Object readResolve() {
			return EMPTY_MAP;
		}
	}
}
