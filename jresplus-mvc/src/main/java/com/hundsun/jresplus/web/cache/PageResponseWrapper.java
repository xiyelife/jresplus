package com.hundsun.jresplus.web.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hundsun.jresplus.web.cache.PageHeader.Type;

public class PageResponseWrapper extends HttpServletResponseWrapper implements
		Serializable {
	/**  */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory
			.getLogger(PageResponseWrapper.class);
	private int statusCode = SC_OK;
	private int contentLength;
	private String contentType;
	private final Map<String, List<Serializable>> headersMap = new TreeMap<String, List<Serializable>>(
			String.CASE_INSENSITIVE_ORDER);
	private final List<Cookie> cookies = new ArrayList<Cookie>();
	private ServletOutputStream outstr;
	private PrintWriter writer;
	private boolean disableFlushBuffer = true;

	public PageResponseWrapper(HttpServletResponse response, OutputStream outstr) {
		super(response);
		this.outstr = new CustomServletOutputStream(outstr);
	}

	public ServletOutputStream getOutputStream() {
		return this.outstr;
	}

	public void setStatus(int code) {
		this.statusCode = code;
		super.setStatus(code);
	}

	public void sendError(int code, String string) throws IOException {
		this.statusCode = code;
		super.sendError(code, string);
	}

	public void sendError(int code) throws IOException {
		this.statusCode = code;
		super.sendError(code);
	}

	public void sendRedirect(String string) throws IOException {
		this.statusCode = HttpServletResponse.SC_MOVED_TEMPORARILY;
		super.sendRedirect(string);
	}

	public void setStatus(int code, String msg) {
		this.statusCode = code;
		LOG.warn("Discarding message because this method is deprecated.");
		super.setStatus(code);
	}

	public int getStatus() {
		return this.statusCode;
	}

	public void setContentLength(int length) {
		this.contentLength = length;
		super.setContentLength(length);
	}

	public int getContentLength() {
		return this.contentLength;
	}

	public void setContentType(String type) {
		this.contentType = type;
		super.setContentType(type);
	}

	public String getContentType() {
		return this.contentType;
	}

	public PrintWriter getWriter() throws IOException {
		if (this.writer == null) {
			this.writer = new PrintWriter(new OutputStreamWriter(this.outstr,
					getCharacterEncoding()), true);
		}
		return this.writer;
	}

	public void addHeader(String name, String value) {
		List<Serializable> values = this.headersMap.get(name);
		if (values == null) {
			values = new LinkedList<Serializable>();
			this.headersMap.put(name, values);
		}
		values.add(value);

		super.addHeader(name, value);
	}

	public void setHeader(String name, String value) {
		LinkedList<Serializable> values = new LinkedList<Serializable>();
		values.add(value);
		this.headersMap.put(name, values);

		super.setHeader(name, value);
	}

	public void addDateHeader(String name, long date) {
		List<Serializable> values = (List<Serializable>) this.headersMap
				.get(name);
		if (values == null) {
			values = new LinkedList<Serializable>();
			this.headersMap.put(name, values);
		}
		values.add(date);

		super.addDateHeader(name, date);
	}

	public void setDateHeader(String name, long date) {
		LinkedList<Serializable> values = new LinkedList<Serializable>();
		values.add(date);
		this.headersMap.put(name, values);

		super.setDateHeader(name, date);
	}

	public void addIntHeader(String name, int value) {
		List<Serializable> values = (List<Serializable>) this.headersMap
				.get(name);
		if (values == null) {
			values = new LinkedList<Serializable>();
			this.headersMap.put(name, values);
		}
		values.add(value);

		super.addIntHeader(name, value);
	}

	public void setIntHeader(String name, int value) {
		LinkedList<Serializable> values = new LinkedList<Serializable>();
		values.add(value);
		this.headersMap.put(name, values);

		super.setIntHeader(name, value);
	}

	public Collection<PageHeader<? extends Serializable>> getAllHeaders() {
		List<PageHeader<? extends Serializable>> headers = new LinkedList<PageHeader<? extends Serializable>>();

		for (Map.Entry<String, List<Serializable>> headerEntry : headersMap
				.entrySet()) {
			String name = headerEntry.getKey();
			for (Serializable value : headerEntry.getValue()) {

				if (value != null) {

					Type type = PageHeader.Type.determineType(value.getClass());
					switch (type) {
					case STRING:
						headers.add(new PageHeader<String>(name, (String) value));
						break;
					case DATE:
						headers.add(new PageHeader<Long>(name, (Long) value));
						break;
					case INT:
						headers.add(new PageHeader<Integer>(name,
								(Integer) value));
						break;
					default:
						throw new IllegalArgumentException(
								"No mapping for Header.Type: " + type);
					}
				}
			}
		}

		return headers;
	}

	public void addCookie(Cookie cookie) {
		this.cookies.add(cookie);
		super.addCookie(cookie);
	}

	public Collection<Cookie> getCookies() {
		return this.cookies;
	}

	public void flushBuffer() throws IOException {
		flush();

		if (!this.disableFlushBuffer)
			super.flushBuffer();
	}

	public void reset() {
		super.reset();
		this.cookies.clear();
		this.headersMap.clear();
		this.statusCode = SC_OK;
		this.contentType = null;
		this.contentLength = 0;
	}

	public void resetBuffer() {
		super.resetBuffer();
	}

	public void flush() throws IOException {
		if (this.writer != null) {
			this.writer.flush();
		}
		this.outstr.flush();
	}

	public boolean isDisableFlushBuffer() {
		return this.disableFlushBuffer;
	}

	public void setDisableFlushBuffer(boolean disableFlushBuffer) {
		this.disableFlushBuffer = disableFlushBuffer;
	}

	class CustomServletOutputStream extends ServletOutputStream {
		private OutputStream stream;

		public CustomServletOutputStream(OutputStream stream) {
			this.stream = stream;
		}

		public void write(int b) throws IOException {
			this.stream.write(b);
		}

		public void write(byte[] b) throws IOException {
			this.stream.write(b);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			this.stream.write(b, off, len);
		}
	}
}
