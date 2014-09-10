package com.hundsun.jresplus.web.servlet;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

public class MediaTypesHandler {

	public final static String ATTRIBUTE_NAME = "_MediaTypes";

	private MediaType defaultContentType;

	private boolean favorParameter = true;

	private String parameterName = "format";

	private static final String ACCEPT_HEADER = "Accept";

	private boolean favorPathExtension = true;

	private boolean ignoreAcceptHeader = false;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	protected static final Log logger = LogFactory
			.getLog(MediaTypesHandler.class);

	private ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<String, MediaType>();

	/**
	 * Indicates whether the extension of the request path should be used to
	 * determine the requested media type, in favor of looking at the
	 * {@code Accept} header. The default value is {@code true}.
	 * <p>
	 * For instance, when this flag is <code>true</code> (the default), a
	 * request for {@code /hotels.pdf} will result in an {@code AbstractPdfView}
	 * being resolved, while the {@code Accept} header can be the
	 * browser-defined {@code text/html,application/xhtml+xml}.
	 */
	public void setFavorPathExtension(boolean favorPathExtension) {
		this.favorPathExtension = favorPathExtension;
	}

	/**
	 * Sets the parameter name that can be used to determine the requested media
	 * type if the {@link #setFavorParameter(boolean)} property is {@code true}.
	 * The default parameter name is {@code format}.
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * Indicates whether a request parameter should be used to determine the
	 * requested media type, in favor of looking at the {@code Accept} header.
	 * The default value is {@code false}.
	 * <p>
	 * For instance, when this flag is <code>true</code>, a request for
	 * {@code /hotels?format=pdf} will result in an {@code AbstractPdfView}
	 * being resolved, while the {@code Accept} header can be the
	 * browser-defined {@code text/html,application/xhtml+xml}.
	 */
	public void setFavorParameter(boolean favorParameter) {
		this.favorParameter = favorParameter;
	}

	/**
	 * Sets the default content type.
	 * <p>
	 * This content type will be used when file extension, parameter, nor
	 * {@code Accept} header define a content-type, either through being
	 * disabled or empty.
	 */
	public void setDefaultContentType(MediaType defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	/**
	 * Indicates whether the HTTP {@code Accept} header should be ignored.
	 * Default is {@code false}. If set to {@code true}, this view resolver will
	 * only refer to the file extension and/or paramter, as indicated by the
	 * {@link #setFavorPathExtension(boolean) favorPathExtension} and
	 * {@link #setFavorParameter(boolean) favorParameter} properties.
	 */
	public void setIgnoreAcceptHeader(boolean ignoreAcceptHeader) {
		this.ignoreAcceptHeader = ignoreAcceptHeader;
	}

	/**
	 * Sets the mapping from file extensions to media types.
	 * <p>
	 * When this mapping is not set or when an extension is not present, this
	 * view resolver will fall back to using a {@link FileTypeMap} when the Java
	 * Action Framework is available.
	 */
	public void setMediaTypes(Map<String, String> mediaTypes) {
		Assert.notNull(mediaTypes, "'mediaTypes' must not be null");
		for (Map.Entry<String, String> entry : mediaTypes.entrySet()) {
			String extension = entry.getKey().toLowerCase(Locale.ENGLISH);
			MediaType mediaType = MediaType.parseMediaType(entry.getValue());
			this.mediaTypes.put(extension, mediaType);
		}
	}

	@SuppressWarnings("unchecked")
	public List<MediaType> getMediaTypes(HttpServletRequest request) {
		if (request.getAttribute(ATTRIBUTE_NAME) == null) {
			request.setAttribute(ATTRIBUTE_NAME, getMediaType(request));
		}
		return (List<MediaType>) request.getAttribute(ATTRIBUTE_NAME);
	}

	/**
	 * Determines the list of {@link MediaType} for the given
	 * {@link HttpServletRequest}.
	 * <p>
	 * The default implementation invokes
	 * {@link #getMediaTypeFromFilename(String)} if
	 * {@linkplain #setFavorPathExtension(boolean) favorPathExtension} property
	 * is <code>true</code>. If the property is <code>false</code>, or when a
	 * media type cannot be determined from the request path, this method will
	 * inspect the {@code Accept} header of the request.
	 * <p>
	 * This method can be overriden to provide a different algorithm.
	 * 
	 * @param request
	 *            the current servlet request
	 * @return the list of media types requested, if any
	 */
	private List<MediaType> getMediaType(HttpServletRequest request) {

		if (this.favorPathExtension) {
			String requestUri = urlPathHelper.getRequestUri(request);
			String filename = WebUtils
					.extractFullFilenameFromUrlPath(requestUri);
			MediaType mediaType = getMediaTypeFromFilename(filename);
			if (mediaType != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Requested media type is '" + mediaType
							+ "' (based on filename '" + filename + "')");
				}
				return Collections.singletonList(mediaType);
			}
		}
		if (this.favorParameter) {
			if (request.getParameter(this.parameterName) != null) {
				String parameterValue = request
						.getParameter(this.parameterName);
				MediaType mediaType = getMediaTypeFromParameter(parameterValue);
				if (mediaType != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Requested media type is '" + mediaType
								+ "' (based on parameter '"
								+ this.parameterName + "'='" + parameterValue
								+ "')");
					}
					return Collections.singletonList(mediaType);
				}
			}
		}
		if (!this.ignoreAcceptHeader) {
			String acceptHeader = request.getHeader(ACCEPT_HEADER);
			if (StringUtils.hasText(acceptHeader)) {
				List<MediaType> mediaTypes = MediaType
						.parseMediaTypes(acceptHeader);
				MediaType.sortByQualityValue(mediaTypes);
				if (logger.isDebugEnabled()) {
					logger.debug("Requested media types are " + mediaTypes
							+ " (based on Accept header)");
				}
				return mediaTypes;
			}
		}
		if (this.defaultContentType != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Requested media types is "
						+ this.defaultContentType
						+ " (based on defaultContentType property)");
			}
			return Collections.singletonList(this.defaultContentType);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Determines the {@link MediaType} for the given parameter value.
	 * <p>
	 * The default implementation will check the
	 * {@linkplain #setMediaTypes(Map) media types} property for a defined
	 * mapping.
	 * <p>
	 * This method can be overriden to provide a different algorithm.
	 * 
	 * @param parameterValue
	 *            the parameter value (i.e. {@code pdf}).
	 * @return the media type, if any
	 */
	protected MediaType getMediaTypeFromParameter(String parameterValue) {
		return this.mediaTypes.get(parameterValue.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Determines the {@link MediaType} for the given filename.
	 * <p>
	 * The default implementation will check the
	 * {@linkplain #setMediaTypes(Map) media types} property first for a defined
	 * mapping. If not present, and if the Java Activation Framework can be
	 * found on the classpath, it will call
	 * {@link FileTypeMap#getContentType(String)}
	 * <p>
	 * This method can be overriden to provide a different algorithm.
	 * 
	 * @param filename
	 *            the current request file name (i.e. {@code hotels.html})
	 * @return the media type, if any
	 */
	protected MediaType getMediaTypeFromFilename(String filename) {
		String extension = StringUtils.getFilenameExtension(filename);
		if (!StringUtils.hasText(extension)) {
			return null;
		}
		extension = extension.toLowerCase(Locale.ENGLISH);
		return this.mediaTypes.get(extension);
	}
}
