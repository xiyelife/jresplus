package com.hundsun.jresplus.web.cache;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hundsun.jresplus.beans.ObjectFactory;
import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * 功能说明: 页面静态化过滤器
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-22 <br>
 * 功能描述:
 * 通过配置该过滤器器（web.xml）可以实现动态页面静态化到缓存（需要注入缓存实现）中，后续再次访问该动态页面时直接从缓存中输出，提高页面响应.<br>
 * </p>
 * <p>
 * 必要条件：
 * <li>仅支持GET请求</li>
 * <li>过滤器本身不提供缓存的实现，需要通过spring bean方式注入，注入方式为约定id的方式</li>
 * <li>默认要求注入的页面缓存实现PageStaticCache的bean id名为"pageStaticCache",但可以通过参数化的方式指定</li>
 * </p>
 * <p>
 * 支持的参数配置:<br/>
 * <li>
 * page.static.request.variable-控制层在处理视图响应时，可以在request的attribute中设置一个约定变量，
 * 用于描述该请求对应的视图需要建立静态缓存处理，后续有相同的请求时直接从缓存中返回视,默认约定变量为"pageCache"</li>
 * <li>page.static.nocache.variable-页面请求时可以约定一个请求参数做为控制本次请求是否不进行缓存处理（不从缓存读取，
 * 请求后的结果也不加入到缓存中），默认约定变量为"_nocache"</li>
 * <li>page.static.refresh.variable-页面请求时可以约定一个请求参数做为是否强制刷新（服务端请求动态内容，并将内容缓存），
 * 默认约定变量为"_refresh"</li>
 * <li>
 * page.static.cache.bean-如果要使用页面静态化，必须提供页面静态缓存的实现，并通过约定id的bean进行注入，默认约定的bean
 * id是"pageStaticCache"</li>
 * </p>
 */
public class PageStaticFilter extends OncePerRequestFilter {
	private static final String TRUE_STR = "true";
	public static final String DEF_PAGE_CACHE_BEAN = "pageStaticCache";
	public static final String DEF_NOCACHE_VARIABLE = "_nocache";
	public static final String DEF_REFRESH_VARIABLE = "_refreshcache";
	public static final int DEF_MAX_LIVE_TIME = 120;
	public static final int DEF_MAX_DLE_TIME = 120;
	private static final Logger logger = LoggerFactory
			.getLogger(PageStaticFilter.class);

	public static final String PAGE_STATIC_CACHE = "_page_static_cache";
	public static final String PAGE_STATIC_TIME_TO_LIVE = "_page_static_time_to_live";
	public static final String PAGE_STATIC_TIME_TO_DLE = "_page_static_time_to_dle";

	@Value("${page.static.nocache.variable}")
	private String nocachaeVariable;
	@Value("${page.static.refresh.variable}")
	private String refreshVariable;
	@Value("${page.static.cache.bean}")
	private String cacheBean;
	@Value("${page.static.enable}")
	private String enableStr;
	@Value("${page.static.gzipped}")
	private String gzippedStr;
	private boolean enable = false;
	private boolean gzipped = false;

	private int timeToLiveSeconds = DEF_MAX_LIVE_TIME;
	private int timeToDleSeconds = DEF_MAX_DLE_TIME;
	private boolean onlyEnabledConfiguredURL = false;

	private List<String> urlList = new ArrayList<String>();
	private PageStaticCache pageCache;
	/**
	 * url校验匹配器，支撑 ?：单个字符 *：多个字符，不能跨目录/ **：多个字符，且跨目录
	 */
	private PathMatcher pathMatcher = new AntPathMatcher();
	@Autowired
	ObjectFactory objectFactory;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		request.setAttribute(PAGE_STATIC_CACHE, pageCache);
		request.setAttribute(PAGE_STATIC_TIME_TO_LIVE, timeToLiveSeconds);
		request.setAttribute(PAGE_STATIC_TIME_TO_DLE, timeToDleSeconds);
		if (isNeedStatic(request)) {
			pageStatic(request, response, filterChain);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean isNeedStatic(HttpServletRequest request) {
		if (!enable) {
			return false;
		}
		String method = request.getMethod();
		if (!"GET".equals(method)) {
			return false;
		}
		String nocache = request.getParameter(nocachaeVariable);
		if (nocache != null && TRUE_STR.equals(nocache)) {
			return false;
		}
		if (onlyEnabledConfiguredURL) {
			return isMatchConfiguredURL(request);
		}
		return true;
	}

	private boolean isMatchConfiguredURL(HttpServletRequest request) {
		String key = getRequestKey(request);
		for (String url : urlList) {
			String pattern = url;
			if (url.indexOf(",") > -1) {
				pattern = url.substring(0, url.indexOf(","));
			}
			boolean isMatch = pathMatcher.match(pattern, key);
			if (isMatch) {
				return true;
			}
		}
		return false;
	}

	private void pageStatic(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		PageImage page = buildPageWithCache(request, response, filterChain);
		if (page != null && page.isOk()) {
			writeResponse(request, response, page);
		}
	}

	private PageImage buildPageWithCache(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		String key = getRequestKey(request);

		PageImage pageImage = pageCache.get(key);
		if (pageImage == null || isNeedRefresh(request)) {
			try {

				pageImage = buildPageImage(request, response, filterChain);
				setIime(key, pageImage);
				if (pageImage.isOk()) {
					pageCache.put(key, pageImage);
				} else {
					pageCache.put(key, null);
				}
			} catch (Exception e) {
				logger.error("page render from static cache faild.", e);
				pageCache.put(key, null);
				return null;
			}
		}
		return pageImage;
	}

	private void setIime(String key, PageImage pageImage) {
		int maxLiveTime = timeToLiveSeconds;
		int maxDleTime = timeToDleSeconds;
		for (String url : urlList) {
			String pattern = url;
			int index = url.indexOf(",");
			String[] array = null;
			if (index > -1) {
				pattern = url.substring(0, index);
				array = url.split(",");
			}
			if (StringUtil.isBlank(pattern)) {
				continue;
			}
			boolean isMatch = pathMatcher.match(pattern, key);
			if (!isMatch) {
				continue;
			}
			if (array == null) {
				continue;
			}
			try {
				if (array.length > 1) {
					maxLiveTime = Integer.valueOf(array[1]);
				}
				if (array.length > 2) {
					maxDleTime = Integer.valueOf(array[2]);
				}

			} catch (Exception e) {
				logger.error("max live time must be int.[{}]", url);
			}
		}
		pageImage.setTimeToLiveSeconds(maxLiveTime);
		pageImage.setTimeToDleSeconds(maxDleTime);
	}

	public void setTimeToDleSeconds(int timeToDleSeconds) {
		this.timeToDleSeconds = timeToDleSeconds;
	}

	public PageImage buildPageImage(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PageResponseWrapper wrapper = new PageResponseWrapper(response,
				outStream);
		chain.doFilter(request, wrapper);
		wrapper.flush();
		return new PageImage(wrapper.getStatus(), wrapper.getContentType(),
				wrapper.getCookies(), wrapper.getAllHeaders(),
				outStream.toByteArray(), gzipped);
	}

	protected void writeResponse(HttpServletRequest request,
			HttpServletResponse response, PageImage pageInfo)
			throws IOException {
		boolean requestAcceptsGzipEncoding = acceptsGzipEncoding(request);

		setStatus(response, pageInfo);
		setContentType(response, pageInfo);
		setCookies(pageInfo, response);

		setHeaders(pageInfo, requestAcceptsGzipEncoding, response);
		writeContent(request, response, pageInfo);
	}

	protected void setContentType(HttpServletResponse response,
			PageImage pageInfo) {
		String contentType = pageInfo.getContentType();
		if ((contentType != null) && (contentType.length() > 0))
			response.setContentType(contentType);
	}

	protected void setCookies(PageImage pageInfo, HttpServletResponse response) {
		Collection<PageCookie> cookies = pageInfo.getCookies();
		for (Iterator<PageCookie> iterator = cookies.iterator(); iterator
				.hasNext();) {
			Cookie cookie = iterator.next().toCookie();

			response.addCookie(cookie);
		}
	}

	protected void setStatus(HttpServletResponse response, PageImage pageInfo) {
		response.setStatus(pageInfo.getStatusCode());
	}

	protected void setHeaders(PageImage pageInfo,
			boolean requestAcceptsGzipEncoding, HttpServletResponse response) {
		Collection<PageHeader<? extends Serializable>> headers = pageInfo
				.getHeaders();

		TreeSet<String> setHeaders = new TreeSet<String>(
				String.CASE_INSENSITIVE_ORDER);

		for (PageHeader<? extends Serializable> header : headers) {
			String name = header.getName();
			switch (header.getType().ordinal()) {
			case 1:
				if (setHeaders.contains(name)) {
					response.addHeader(name, (String) header.getValue());
				} else {
					setHeaders.add(name);
					response.setHeader(name, (String) header.getValue());
				}
				break;
			case 2:
				if (setHeaders.contains(name)) {
					response.addDateHeader(name,
							((Long) header.getValue()).longValue());
				} else {
					setHeaders.add(name);
					response.setDateHeader(name,
							((Long) header.getValue()).longValue());
				}
				break;
			case 3:
				if (setHeaders.contains(name)) {
					response.addIntHeader(name,
							((Integer) header.getValue()).intValue());
				} else {
					setHeaders.add(name);
					response.setIntHeader(name,
							((Integer) header.getValue()).intValue());
				}
				break;
			default:
				throw new IllegalArgumentException("No mapping for Header: "
						+ header);
			}
		}
	}

	protected boolean acceptsEncoding(HttpServletRequest request, String name) {
		boolean accepts = headerContains(request, "Accept-Encoding", name);
		return accepts;
	}

	private boolean headerContains(HttpServletRequest request, String header,
			String value) {

		Enumeration accepted = request.getHeaders(header);
		while (accepted.hasMoreElements()) {
			String headerValue = (String) accepted.nextElement();
			if (headerValue.indexOf(value) != -1) {
				return true;
			}
		}
		return false;
	}

	protected boolean acceptsGzipEncoding(HttpServletRequest request) {
		return acceptsEncoding(request, "gzip");
	}

	protected void writeContent(HttpServletRequest request,
			HttpServletResponse response, PageImage pageInfo)
			throws IOException {
		boolean shouldBodyBeZero = ResponseUtil.shouldBodyBeZero(request,
				pageInfo.getStatusCode());
		byte[] body;
		if (shouldBodyBeZero) {
			body = new byte[0];
		} else if (acceptsGzipEncoding(request) && gzipped) {
			body = pageInfo.getGzippedBody();
			if (ResponseUtil.shouldGzippedBodyBeZero(body, request))
				body = new byte[0];
			else
				ResponseUtil.addGzipHeader(response);
		} else {
			body = pageInfo.getUngzippedBody();
		}

		response.setContentLength(body.length);
		OutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(body);
		out.flush();
	}

	private String getRequestKey(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		String key = uri;
		if (queryString != null) {
			key = key + "?" + queryString;
		}
		return key;
	}

	private boolean isNeedRefresh(HttpServletRequest request) {
		String refresh = request.getParameter(refreshVariable);
		if (refresh != null && TRUE_STR.equals(refresh)) {
			return true;
		}
		return false;
	}

	protected void initFilterBean() throws ServletException {
		super.initFilterBean();
		if (nocachaeVariable == null || nocachaeVariable.indexOf("${") > -1) {
			nocachaeVariable = DEF_NOCACHE_VARIABLE;
		}
		if (refreshVariable == null || refreshVariable.indexOf("${") > -1) {
			refreshVariable = DEF_REFRESH_VARIABLE;
		}
		if (cacheBean == null || cacheBean.indexOf("${") > -1) {
			cacheBean = DEF_PAGE_CACHE_BEAN;
		}

		if (enableStr == null || enableStr.indexOf("${") > -1) {
			enableStr = TRUE_STR;
		}
		if (TRUE_STR.equals(enableStr)) {
			enable = true;
		}
		if (TRUE_STR.equals(gzippedStr)) {
			gzipped = true;
		}
		if (pageCache == null) {
			Object bean = objectFactory.getBean(cacheBean);
			if (bean instanceof PageStaticCache) {
				pageCache = (PageStaticCache) bean;
			}
		}

		Assert.notNull(pageCache,
				"Use page static must be has PageStaticCache impl[id=("
						+ cacheBean + ") inject] or config ref injectj");
		logger.info("Page static use PageStaticCache impl[{}]",
				pageCache.getClass());
	}

	public void setNocachaeVariable(String nocachaeVariable) {
		this.nocachaeVariable = nocachaeVariable;
	}

	public void setRefreshVariable(String refreshVariable) {
		this.refreshVariable = refreshVariable;
	}

	public void setCacheBean(String cacheBean) {
		this.cacheBean = cacheBean;
	}

	public void setUrlList(List<String> urlList) {
		this.urlList = urlList;
	}

	public void setPageCache(PageStaticCache pageCache) {
		this.pageCache = pageCache;
	}

	public boolean isOnlyEnabledConfiguredURL() {
		return onlyEnabledConfiguredURL;
	}

	public void setOnlyEnabledConfiguredURL(boolean onlyEnabledConfiguredURL) {
		this.onlyEnabledConfiguredURL = onlyEnabledConfiguredURL;
	}

	public void setTimeToLiveSeconds(int timeToLiveSeconds) {
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

}
