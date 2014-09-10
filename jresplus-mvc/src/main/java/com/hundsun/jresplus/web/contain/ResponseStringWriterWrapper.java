package com.hundsun.jresplus.web.contain;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class ResponseStringWriterWrapper extends HttpServletResponseWrapper {
	private StringWriter sw = new StringWriter();

	public ResponseStringWriterWrapper(HttpServletResponse r) {
		super(r);
	}

	public StringWriter getStringWriter() {
		return sw;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				sw.write(b);
			}
		};
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(sw);
	}

	@Override
	public void reset() {
		sw = new StringWriter();
	}

	@Override
	public void resetBuffer() {
		sw = new StringWriter();
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void flushBuffer() throws IOException {

	}

	@Override
	public void setBufferSize(int arg0) {

	}

	@Override
	public void setCharacterEncoding(String arg0) {

	}

	@Override
	public void setContentLength(int arg0) {

	}

	@Override
	public void setLocale(Locale arg0) {

	}

	@Override
	public void addDateHeader(String arg0, long arg1) {

	}

	@Override
	public void addHeader(String arg0, String arg1) {

	}

	@Override
	public void addIntHeader(String arg0, int arg1) {

	}

	@Override
	public void setDateHeader(String arg0, long arg1) {

	}

	@Override
	public void setHeader(String arg0, String arg1) {

	}

	@Override
	public void setIntHeader(String arg0, int arg1) {

	}

	@Override
	public void setStatus(int arg0, String arg1) {

	}

	@Override
	public void setStatus(int arg0) {

	}
}