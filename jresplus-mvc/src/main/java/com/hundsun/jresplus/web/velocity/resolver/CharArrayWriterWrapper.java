package com.hundsun.jresplus.web.velocity.resolver;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Renderable;

import com.hundsun.jresplus.common.util.io.CharArrayWriter;


/**
 * 
 * @author fish
 * 
 */
public class CharArrayWriterWrapper implements Renderable {
	private CharArrayWriter charArrayWriter = new CharArrayWriter();

	public Writer getWriter() {
		return this.charArrayWriter;
	}

	public void reset() {
		this.charArrayWriter.reset();
	}

	public void writeTo(Writer out) throws IOException {
		this.charArrayWriter.writeTo(out);
	}

	public boolean render(InternalContextAdapter adapter, Writer writer)
			throws IOException, MethodInvocationException, ParseErrorException,
			ResourceNotFoundException {
		charArrayWriter.writeTo(writer);
		return true;
	}
}
