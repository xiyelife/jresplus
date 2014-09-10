/*
 * 修改日期            修改人员                     修改说明 <br>
 * ========    =======  ============================================
 * 20140210		hanyin	STORY #7560 [研发中心/内部需求][jresplus-mvc]-URLBroker支持动态IP和端口支持
 * ========    =======  ============================================
 */
package com.hundsun.jresplus.web.url;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hundsun.jresplus.web.velocity.eventhandler.DirectOutput;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class URLBroker {
	protected final Log logger = LogFactory.getLog(this.getClass());

	protected String encoding = "UTF-8";

	protected URLConfig config;

	protected String contextPath;
	protected String name;

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public URLConfig getConfig() {
		return config;
	}

	public void setConfig(URLConfig config) {
		this.config = config;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected String server = null;

	public void init(String contextPath) {
		this.contextPath = contextPath;
		if (this.server != null) {
			return;
		}
		StringBuilder server = new StringBuilder();
		server.append(config.getURL());
		if (config.isFollowContextPath()) {
			server.append(contextPath);
		}
		/** { 20140210 begin add hanyin STORY #7560 增加非空判断 */
		if (server.length() > 0 && server.charAt(server.length() - 1) == '/') {
			server.deleteCharAt(server.length() - 1);
		}
		/** } 20140210 end add hanyin STORY #7560 */

		if (StringUtils.isNotBlank(config.getPath())) {
			if (config.getPath().startsWith("/")) {
				server.append(config.getPath());
			} else {
				server.append('/').append(config.getPath());
			}
		}

		// { 20140210 begin add hanyin STORY #7560 增加非空判断
		if (server.length() > 0 && server.charAt(server.length() - 1) == '/') {
			server.deleteCharAt(server.length() - 1);
		}
		// } 20140210 end add hanyin STORY #7560

		this.server = server.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("init end,server:" + this.server);
		}
	}

	public QueryData setTarget(String target) {
		return new QueryData(target);
	}

	public QueryData get(String target) {
		return setTarget(target);
	}

	@Override
	public String toString() {
		return this.server;
	}

	public class QueryData implements DirectOutput {
		protected StringBuilder query;

		protected QueryData() {
		}

		protected QueryData(String target) {
			if (target == null) {
				return;
			}
			query = new StringBuilder();
			query.append(URLBroker.this.server);
			if (target.startsWith("/")) {
				query.append(target);
			} else {
				query.append('/').append(target);
			}
			query.append('?');
		}

		public QueryData addNullQueryData(String id) {
			query.append(id).append('=').append('&');
			return this;
		}

		public QueryData putNull(String id) {
			return addNullQueryData(id);
		}

		public QueryData addQueryData(String id, String value) {
			if (value == null) {
				return addNullQueryData(id);
			}
			query.append(id).append('=');
			try {
				query.append(URLEncoder.encode(value, URLBroker.this.encoding));
			} catch (UnsupportedEncodingException e) {
				if (logger.isErrorEnabled()) {
					logger.error("UnsupportedEncoding:"
							+ URLBroker.this.encoding, e);
				}
			}
			query.append('&');
			return this;
		}

		public QueryData put(String id, String value) {
			return addQueryData(id, value);
		}

		public QueryData addQueryData(String id, long value) {
			return addQueryData(id, String.valueOf(value));
		}

		public QueryData put(String id, long value) {
			return addQueryData(id, value);
		}

		public QueryData addQueryData(String id, Object value) {
			if (value == null) {
				return addNullQueryData(id);
			}
			if (value.getClass().isArray()) {
				int length = Array.getLength(value);
				for (int i = 0; i < length; i++) {
					Object one = Array.get(value, i);
					addQueryData(id, one);
				}
				return this;
			} else {
				return addQueryData(id, String.valueOf(value));
			}
		}

		public QueryData put(String id, Object value) {
			return addQueryData(id, value);
		}

		public QueryData addQueryData(String id, double value) {
			return addQueryData(id, String.valueOf(value));
		}

		public QueryData put(String id, double value) {
			return addQueryData(id, value);
		}

		public QueryData addQueryData(String id, int value) {
			return addQueryData(id, String.valueOf(value));
		}

		public QueryData put(String id, int value) {
			return addQueryData(id, value);
		}

		public QueryData addQueryDatas(Map<String, Object> parameters) {
			if (parameters == null || parameters.isEmpty()) {
				return this;
			}
			for (Entry<String, Object> entry : parameters.entrySet()) {
				this.addQueryData(entry.getKey(), entry.getValue());
			}
			return this;
		}

		public QueryData putAll(Map<String, Object> parameters) {
			return addQueryDatas(parameters);
		}

		@Override
		public String toString() {
			if (this.query == null) {
				return URLBroker.this.toString();
			}
			char last = this.query.charAt(this.query.length() - 1);
			if (last == '?' || last == '&') {
				return this.query.deleteCharAt(this.query.length() - 1)
						.toString();
			}
			return this.query.toString();
		}

	}

}
