package com.hundsun.jresplus.web.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 页面镜像，用于描述响应的页面信息，该页面镜像做为存储到缓存中的值，同时从缓存中读取后用于还原响应过程
 * @author XIE
 *
 */
public class PageImage implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(PageImage.class);
	private static final int FOUR_KB = 4196;
	private static final int GZIP_MAGIC_NUMBER_BYTE_1 = 31;
	private static final int GZIP_MAGIC_NUMBER_BYTE_2 = -117;
	private final ArrayList<PageHeader<? extends Serializable>> headers = new ArrayList<PageHeader<? extends Serializable>>();
	private final ArrayList<PageCookie> cookies = new ArrayList<PageCookie>();
	private String contentType;
	private byte[] gzippedBody;
	private byte[] ungzippedBody;
	private String snippet;
	private int statusCode;
	private boolean storeGzipped;
	private Date created;
	private int timeToLiveSeconds;
	private int timeToDleSeconds;

	public PageImage(int statusCode, String contentType,
			Collection<Cookie> cookies,
			Collection<PageHeader<? extends Serializable>> headers,
			byte[] body, boolean storeGzipped) {
		init(statusCode, contentType, headers, cookies, body, storeGzipped);
	}

	public PageImage(String snippet) {
		this.snippet = snippet;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public int getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	public int getTimeToDleSeconds() {
		return timeToDleSeconds;
	}

	public void setTimeToDleSeconds(int timeToDleSeconds) {
		this.timeToDleSeconds = timeToDleSeconds;
	}

	public void setTimeToLiveSeconds(int timeToLiveSeconds) {
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	private void init(int statusCode, String contentType,
			Collection<PageHeader<? extends Serializable>> headers,
			Collection<Cookie> cookies, byte[] body, boolean storeGzipped) {
		if (headers != null) {
			this.headers.addAll(headers);
		}

		this.created = new Date();
		this.contentType = contentType;
		this.storeGzipped = storeGzipped;
		this.statusCode = statusCode;
		try {
			if (storeGzipped) {
				this.ungzippedBody = null;
				if (isBodyParameterGzipped())
					this.gzippedBody = body;
				else
					this.gzippedBody = gzip(body);
			} else {
				if (isBodyParameterGzipped()) {
					throw new IllegalArgumentException(
							"Non gzip content has been gzipped.");
				}
				this.ungzippedBody = body;
			}
		} catch (IOException e) {
			LOG.error("Error ungzipping gzipped body", e);
		}
	}

	private byte[] gzip(byte[] ungzipped) throws IOException {
		if (isGzipped(ungzipped)) {
			return ungzipped;
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bytes);
		gzipOutputStream.write(ungzipped);
		gzipOutputStream.close();
		return bytes.toByteArray();
	}

	private boolean isBodyParameterGzipped() {
		for (PageHeader<? extends Serializable> header : this.headers) {
			if ("gzip".equals(header.getValue())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isGzipped(byte[] candidate) {
		if ((candidate == null) || (candidate.length < 2)) {
			return false;
		}
		return (candidate[0] == GZIP_MAGIC_NUMBER_BYTE_1)
				&& (candidate[1] == GZIP_MAGIC_NUMBER_BYTE_2);
	}

	public String getContentType() {
		return this.contentType;
	}

	public byte[] getGzippedBody() {
		if (this.storeGzipped) {
			return this.gzippedBody;
		}
		return null;
	}

	public List<PageHeader<? extends Serializable>> getHeaders() {
		return this.headers;
	}

	public List<PageCookie> getCookies() {
		return this.cookies;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public byte[] getUngzippedBody() throws IOException {
		if (this.storeGzipped) {
			return ungzip(this.gzippedBody);
		}
		return this.ungzippedBody;
	}

	private byte[] ungzip(byte[] gzipped) throws IOException {
		GZIPInputStream inputStream = new GZIPInputStream(
				new ByteArrayInputStream(gzipped));
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
				gzipped.length);
		byte[] buffer = new byte[FOUR_KB];
		int bytesRead = 0;
		while (bytesRead != -1) {
			bytesRead = inputStream.read(buffer, 0, FOUR_KB);
			if (bytesRead != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
			}
		}
		byte[] ungzipped = byteArrayOutputStream.toByteArray();
		inputStream.close();
		byteArrayOutputStream.close();
		return ungzipped;
	}

	public boolean hasGzippedBody() {
		return this.gzippedBody != null;
	}

	public boolean hasUngzippedBody() {
		return this.ungzippedBody != null;
	}

	public boolean isOk() {
		return this.statusCode == 200;
	}

	public Date getCreated() {
		return this.created;
	}

}
