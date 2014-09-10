package com.hundsun.jresplus.web.url;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class RestUrlBroker extends URLBroker {

	private static final Pattern NAMES_PATTERN = Pattern
			.compile("\\{([^/]+?)\\}");

	private Map<String, UrlTemplate> name2Template;

	public void setUrlTemplates(Map<String, String> templates) {
		if (templates == null || templates.isEmpty()) {
			throw new IllegalArgumentException("templates can't empty.");
		}
		this.name2Template = new HashMap<String, UrlTemplate>(templates.size());
		for (Entry<String, String> entry : templates.entrySet()) {
			UrlTemplate temp = new UrlTemplate(entry.getValue());
			this.name2Template.put(entry.getKey(), temp);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("init RestUrlBroker whih Templates:"
					+ this.name2Template);
		}
	}

	@Override
	public QueryData setTarget(String target) {
		UrlTemplate t = this.name2Template.get(target);
		if (t == null) {
			return super.setTarget(target);
		}
		return new RestQueryData(t);
	}

	public class RestQueryData extends QueryData {
		private UrlTemplate template;

		private Map<String, String> restVariables = new HashMap<String, String>(
				2);

		public RestQueryData(UrlTemplate template) {
			this.template = template;
		}

		@Override
		public QueryData addQueryData(String id, String value) {
			if (this.template.isVariable(id)) {
				restVariables.put(id, value);
			} else {
				if (this.query == null) {
					query = new StringBuilder("?");
				}
				super.addQueryData(id, value);
			}
			return this;
		}

		@Override
		public QueryData addNullQueryData(String id) {
			if (this.template.isVariable(id)) {
				restVariables.put(id, "");
			} else {
				if (this.query == null) {
					query = new StringBuilder("?");
				}
				super.addNullQueryData(id);
			}
			return this;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(RestUrlBroker.this.server);
			sb.append(this.template.merge(restVariables));
			if (this.query != null) {
				sb.append(query);
			}
			char last = sb.charAt(sb.length() - 1);
			if (last == '&') {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		}
	}

	private static class UrlTemplate {
		private String pathTemplate;

		/**
		 */
		private String[] pathSplits;

		/**
		 */
		private String[] variables;

		private UrlTemplate(String pathTemplate) {
			if (!pathTemplate.startsWith("/")) {
				pathTemplate = "/" + pathTemplate;
			}
			this.pathTemplate = pathTemplate;
			this.pathSplits = NAMES_PATTERN.split(pathTemplate);
			if (pathSplits == null || pathSplits.length == 0) {
				throw new IllegalArgumentException(pathTemplate
						+ " is not a vaild url path.");
			}
			if (pathSplits.length == 1) {
				throw new IllegalArgumentException(pathTemplate
						+ " is not a vaild rest url path.");
			}
			List<String> variableNames = new ArrayList<String>();
			Matcher m = NAMES_PATTERN.matcher(pathTemplate);
			while (m.find()) {
				variableNames.add(m.group(1));
			}
			this.variables = variableNames.toArray(new String[variableNames
					.size()]);
		}

		private boolean isVariable(String v) {
			for (String var : this.variables) {
				if (var.equals(v)) {
					return true;
				}
			}
			return false;
		}

		private String merge(Map<String, String> vmap) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.pathSplits.length; i++) {
				sb.append(pathSplits[i]);
				if (i < variables.length) {
					String v = vmap.get(variables[i]);
					if (v == null) {
						sb.append("");
					} else {
						sb.append(v);
					}
				}
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return pathTemplate;
		}

	}

}
