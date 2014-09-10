package com.hundsun.jresplus.web.view;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.alibaba.fastjson.JSON;
/**
 * 基于fastjson的json view，支持编码为UTF-8，支持驼峰转下划线，支持模型转换
 * @author XIE (xjj@hundsun.com)
 *
 */
public class FastJsonView extends AbstractView {
	public static final String DEFAULT_CONTENT_TYPE = "application/json";
	private Set<String> renderedAttributes;
	private String charset = "UTF-8";
	private boolean disableCaching = true;
	private boolean cameToUnderline = false;
	private ModelConverter modelConverter;

	public FastJsonView() {
		setContentType(DEFAULT_CONTENT_TYPE);
	}

	@Override
	protected void prepareResponse(HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType(getContentType());
		response.setCharacterEncoding(charset);
		if (disableCaching) {
			response.addHeader("Pragma", "no-cache");
			response.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
			response.addDateHeader("Expires", 1L);
		}
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String json = "";
		if (modelConverter != null) {
			Model modelObj = modelConverter.convert(filterModel(model));
			json = JSON.toJSONString(modelObj);
		} else {
			json = JSON.toJSONString(filterModel(model));
		}
		if (cameToUnderline) {
			json = camel2underline(json);
		}
		OutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(json.getBytes(charset));
		out.flush();

	}

	protected Map filterModel(Map<String, Object> model) {
		Map<String, Object> result = new HashMap<String, Object>(model.size());
		Set<String> renderedAttributes = !CollectionUtils
				.isEmpty(this.renderedAttributes) ? this.renderedAttributes
				: model.keySet();
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (!(entry.getValue() instanceof BindingResult)
					&& renderedAttributes.contains(entry.getKey())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	public Set<String> getRenderedAttributes() {
		return renderedAttributes;
	}

	public void setRenderedAttributes(Set<String> renderedAttributes) {
		this.renderedAttributes = renderedAttributes;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isDisableCaching() {
		return disableCaching;
	}

	public void setDisableCaching(boolean disableCaching) {
		this.disableCaching = disableCaching;
	}

	public boolean isCameToUnderline() {
		return cameToUnderline;
	}

	public void setCameToUnderline(boolean cameToUnderline) {
		this.cameToUnderline = cameToUnderline;
	}

	public ModelConverter getModelConverter() {
		return modelConverter;
	}

	public void setModelConverter(ModelConverter modelConverter) {
		this.modelConverter = modelConverter;
	}

	private String camel2underline(String param) {
		Pattern p = Pattern.compile("[A-Z]");
		if (param == null || param.equals("")) {
			return "";
		}
		StringBuilder builder = new StringBuilder(param);
		Matcher mc = p.matcher(param);
		int i = 0;
		while (mc.find()) {
			builder.replace(mc.start() + i, mc.end() + i, "_"
					+ mc.group().toLowerCase());
			i++;
		}

		if ('_' == builder.charAt(0)) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
}
