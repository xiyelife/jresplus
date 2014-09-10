package com.hundsun.jresplus.web.url;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class MultiHostURLBroker extends URLBroker {

	private MultiHostURLConfig config;

	@Override
	public QueryData setTarget(String target) {
		return new MultiHostQueryData(target);
	}

	@Override
	public void init(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public void setConfig(URLConfig config) {
		if (config instanceof MultiHostURLConfig) {
			this.config = (MultiHostURLConfig) config;
		} else {
			throw new IllegalArgumentException(
					"MultiHostURLBroker only support MultiHostURLConfig");
		}
	}

	public String toString() {
		return this.getServer(0);
	}

	protected String getServer(int id) {
		StringBuilder server = new StringBuilder();
		server.append(config.getURL(id));
		if (config.isFollowContextPath()) {
			server.append(contextPath);
		}
		if (server.length() > 0 && server.charAt(server.length() - 1) == '/') {
			server.deleteCharAt(server.length() - 1);
		}

		if (StringUtil.isNotBlank(config.getPath())) {
			if (config.getPath().startsWith("/")) {
				server.append(config.getPath());
			} else {
				server.append('/').append(config.getPath());
			}
		}

		if (server.length() > 0 && server.charAt(server.length() - 1) == '/') {
			server.deleteCharAt(server.length() - 1);
		}
		return server.toString();
	}

	/**
	 * @param target
	 * @return
	 */
	protected int getTargetHash(String target) {
		return target.hashCode();
	}

	public class MultiHostQueryData extends QueryData {

		public MultiHostQueryData() {
			super();
		}

		public MultiHostQueryData(String target) {
			if (target == null) {
				return;
			}
			query = new StringBuilder();
			query.append(MultiHostURLBroker.this
					.getServer(getTargetHash(target)));
			if (target.startsWith("/")) {
				query.append(target);
			} else {
				query.append('/').append(target);
			}
			query.append('?');
		}
	}
}
