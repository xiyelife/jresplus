package com.hundsun.jresplus.web.cache;

import java.io.Serializable;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 响应工具类
 * 参考ehcache
 * @author XIE
 *
 */
public class ResponseUtil {
	private static final Logger LOG = LoggerFactory
			.getLogger(ResponseUtil.class);
	private static final int EMPTY_GZIPPED_CONTENT_SIZE = 20;

	public static boolean shouldGzippedBodyBeZero(byte[] compressedBytes,
			HttpServletRequest request) {
		if (compressedBytes.length == EMPTY_GZIPPED_CONTENT_SIZE) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(request.getRequestURL()
						+ " resulted in an empty response.");
			}
			return true;
		}
		return false;
	}

	public static boolean shouldBodyBeZero(HttpServletRequest request,
			int responseStatus) {
		if (responseStatus == 204) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(request.getRequestURL()
						+ " resulted in a "
						+ 204
						+ " response. Removing message body in accordance with RFC2616.");
			}

			return true;
		}

		if (responseStatus == 304) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(request.getRequestURL()
						+ " resulted in a "
						+ 304
						+ " response. Removing message body in accordance with RFC2616.");
			}

			return true;
		}

		return false;
	}

	public static void addGzipHeader(HttpServletResponse response) {

		boolean containsEncoding = response.containsHeader("Content-Encoding");
		if (containsEncoding) {
			response.setHeader("Content-Encoding", "gzip");
		}

	}

	public static void addVaryAcceptEncoding(PageResponseWrapper wrapper) {
		Collection<PageHeader<? extends Serializable>> headers = wrapper
				.getAllHeaders();

		PageHeader<? extends Serializable> varyHeader = null;
		for (PageHeader<? extends Serializable> header : headers) {
			if (header.getName().equals("Vary")) {
				varyHeader = header;
				break;
			}
		}

		if (varyHeader == null) {
			wrapper.setHeader("Vary", "Accept-Encoding");
		} else {
			String varyValue = varyHeader.getValue().toString();
			if ((!varyValue.equals("*"))
					&& (!varyValue.contains("Accept-Encoding")))
				wrapper.setHeader("Vary", varyValue + ",Accept-Encoding");
		}
	}
}
