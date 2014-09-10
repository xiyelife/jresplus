package com.hundsun.jresplus.web.contain.async;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Renderable;

import com.alibaba.fastjson.JSON;
import com.hundsun.jresplus.common.util.ArrayUtil;
import com.hundsun.jresplus.web.contain.ResponseStringWriterWrapper;
import com.hundsun.jresplus.web.velocity.eventhandler.DirectOutput;

/**
 * 
 * @author LeoHu copy by sagahl copy by eyeieye
 * 
 */
public class AsynchronContainTask implements DirectOutput, Renderable {
	public static final String AsynchronContainTaskName = "asyncContainContext";
	private String divId;
	private ResponseStringWriterWrapper responseStringWriter;

	private Set<String> js;
	private StringBuilder jsCode = new StringBuilder();
	private Set<String> css;
	private boolean hasCss = false;

	public boolean hasCss() {
		return this.hasCss;
	}

	public AsynchronContainTask(String divId,
			ResponseStringWriterWrapper responseStringWriter) {
		super();
		this.divId = divId;
		this.responseStringWriter = responseStringWriter;
	}

	public String getDivPlaceholder() {
		return "<div id=\"" + divId + "\"></div>";
	}

	public void writeTo(Writer writer) throws IOException {
		String result = responseStringWriter.getStringWriter().toString();
		writer.write("{\"html\":\"");
		StringEscapeUtils.escapeJavaScript(writer, result);
		writer.write("\",\"id\":\"");
		writer.write(this.getDivId());
		writer.write("\",\"css\":");
		if (ArrayUtil.isEmpty(css)) {
			writer.write("[]");
		} else {
			writer.write(JSON.toJSONString(css));
		}
		writer.write(",\"js\":");
		if (ArrayUtil.isEmpty(js)) {
			writer.write("[]");
		} else {
			writer.write(JSON.toJSONString(js));
		}
		writer.write(",\"jsCode\":\"");
		if (this.jsCode.length() < 1) {
			writer.write("");
		} else {
			writer.write(this.jsCode.toString());
		}
		writer.write("\"}");
	}

	public void addJsCode(String jsCode) throws IOException {
		StringWriter sw = new StringWriter();
		StringEscapeUtils.escapeJavaScript(sw, jsCode);
		this.jsCode.append(sw);
	}

	public AsynchronContainTask addJs(String js) {
		if (this.js == null) {
			this.js = new HashSet<String>();
		}
		this.js.add(js);
		return this;
	}

	public AsynchronContainTask addCss(String css) {
		if (this.css == null) {
			this.css = new HashSet<String>();
		}
		this.css.add(css);
		this.hasCss = true;
		return this;
	}

	public boolean render(InternalContextAdapter arg0, Writer writer)
			throws IOException, MethodInvocationException, ParseErrorException,
			ResourceNotFoundException {
		writer.write("");
		return true;
	}

	public Set<String> getJs() {
		return js;
	}

	public Set<String> getCss() {
		return css;
	}

	public String getDivId() {
		return divId;
	}

	public void setDivId(String divId) {
		this.divId = divId;
	}

	public ResponseStringWriterWrapper getResponseStringWriter() {
		return responseStringWriter;
	}

	public void setResponseStringWriter(
			ResponseStringWriterWrapper responseStringWriter) {
		this.responseStringWriter = responseStringWriter;
	}

}
