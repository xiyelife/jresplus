package com.hundsun.jresplus.common.util.io;

import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class StringBuilderWriter extends Writer {

	private StringBuilder buf;

	public StringBuilderWriter() {
		buf = new StringBuilder();
	}

	public StringBuilderWriter(int initialSize) {
		if (initialSize < 0) {
			throw new IllegalArgumentException("Negative buffer size");
		}
		buf = new StringBuilder(initialSize);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if ((off < 0) || (off > cbuf.length) || (len < 0)
				|| ((off + len) > cbuf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		buf.append(cbuf, off, len);
	}

	@Override
	public void write(int c) throws IOException {
		buf.append((char) c);
	}

	@Override
	public void write(String str) {
		buf.append(str);
	}

	@Override
	public void write(String str, int off, int len) {
		buf.append(str.substring(off, off + len));
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		if (csq == null)
			write("null");
		else
			write(csq.toString());
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end)
			throws IOException {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		write(c);
		return this;
	}

	@Override
	public String toString() {
		return buf.toString();
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

}
