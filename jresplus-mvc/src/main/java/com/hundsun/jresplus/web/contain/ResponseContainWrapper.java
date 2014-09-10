package com.hundsun.jresplus.web.contain;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 *
 */
public class ResponseContainWrapper extends HttpServletResponseWrapper {

	private Writer writer;

	public ResponseContainWrapper(HttpServletResponse r, Writer writer) {
		super(r);
		this.writer = writer;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				writer.write(b);
			}
		};
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(writer);
	}

	@Override
	public void reset() {
	}

	@Override
	public void resetBuffer() {
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
	public void setStatus(int arg0, String arg1) {

	}

	@Override
	public void setStatus(int arg0) {

	}
}
