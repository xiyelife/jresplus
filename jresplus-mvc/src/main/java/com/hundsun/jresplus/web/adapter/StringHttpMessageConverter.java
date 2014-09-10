package com.hundsun.jresplus.web.adapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.util.FileCopyUtils;

public class StringHttpMessageConverter extends
		AbstractHttpMessageConverter<String> {

	private Charset charset;

	private final List<Charset> availableCharsets;

	private boolean writeAcceptCharset = true;

	public StringHttpMessageConverter(String charsetName) {
		super(new MediaType("text", "plain", Charset.forName(charsetName)),
				MediaType.ALL);
		this.charset = Charset.forName(charsetName);
		this.availableCharsets = new ArrayList<Charset>(Charset
				.availableCharsets().values());
	}

	/**
	 * Indicates whether the {@code Accept-Charset} should be written to any
	 * outgoing request.
	 * <p>
	 * Default is {@code true}.
	 */
	public void setWriteAcceptCharset(boolean writeAcceptCharset) {
		this.writeAcceptCharset = writeAcceptCharset;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return String.class.equals(clazz);
	}

	@Override
	protected String readInternal(Class clazz, HttpInputMessage inputMessage)
			throws IOException {
		MediaType contentType = inputMessage.getHeaders().getContentType();
		Charset targetCharset = contentType.getCharSet() != null ? contentType
				.getCharSet() : this.charset;
		return FileCopyUtils.copyToString(new InputStreamReader(inputMessage
				.getBody(), targetCharset));
	}

	@Override
	protected Long getContentLength(String s, MediaType contentType) {
		if (contentType != null && contentType.getCharSet() != null) {
			Charset targetCharset = contentType.getCharSet();
			try {
				return (long) s.getBytes(targetCharset.name()).length;
			} catch (UnsupportedEncodingException ex) {
				// should not occur
				throw new InternalError(ex.getMessage());
			}
		} else {
			return null;
		}
	}

	@Override
	protected void writeInternal(String s, HttpOutputMessage outputMessage)
			throws IOException {
		if (writeAcceptCharset) {
			outputMessage.getHeaders().setAcceptCharset(getAcceptedCharsets());
		}
		MediaType contentType = outputMessage.getHeaders().getContentType();
		Charset targetCharset = contentType.getCharSet() != null ? contentType
				.getCharSet() : this.charset;
		FileCopyUtils.copy(s, new OutputStreamWriter(outputMessage.getBody(),
				targetCharset));
	}

	/**
	 * Return the list of supported {@link Charset}.
	 * 
	 * <p>
	 * By default, returns {@link Charset#availableCharsets()}. Can be
	 * overridden in subclasses.
	 * 
	 * @return the list of accepted charsets
	 */
	protected List<Charset> getAcceptedCharsets() {
		return this.availableCharsets;
	}

}
