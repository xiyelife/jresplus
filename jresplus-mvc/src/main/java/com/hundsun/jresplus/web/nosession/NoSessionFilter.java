/*
 * 修改记录
 * 2013-8-13	 STORY #6534 nosession增加开关配置
 * 2013-9-29 增加cookie方式的session存储单元接口替换原来继承方式的扩展机制
 * 2014-2-7		STORY #7563 -nosession的cookiestore支持domain
 */
package com.hundsun.jresplus.web.nosession;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.lang.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hundsun.jresplus.beans.ObjectFactory;
import com.hundsun.jresplus.common.util.ArrayUtil;
import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.common.util.io.BufferedByteArrayOutputStream;
import com.hundsun.jresplus.web.nosession.cookie.AttributeCookieStore;
import com.hundsun.jresplus.web.nosession.cookie.CookiesManager;
import com.hundsun.jresplus.web.nosession.cookie.Encode;

/**
 * 
 * @author LeoHu copy by sagahl
 * 
 */
@SuppressWarnings("deprecation")
public class NoSessionFilter extends OncePerRequestFilter implements Filter {
	private final static Logger log = LoggerFactory
			.getLogger(NoSessionFilter.class);

	public static final String CREATION_TIME = "creationTime";
	public static final String LAST_ACCESSED_TIME = "lastAccessedTime";
	public static final String SESSION_ID = "jsessionId";

	private static final ThreadLocal<BufferedByteArrayOutputStream> outputStreams = new ThreadLocal<BufferedByteArrayOutputStream>();

	private UUIDGenerator uuidGenerator;

	private int maxInactiveInterval = -1;

	private ObjectFactory objectFactory;

	private ServletContext servletContext;

	private int outBufferSize = 1024 * 5;

	private int recyclingBufferBlockSize = 2;

	List<AttributeCookieStore> attributeCookieStores = new ArrayList<AttributeCookieStore>();

	List<AttributeStore> attributeStores = new ArrayList<AttributeStore>();

	Set<String> enumerations = new HashSet<String>();

	private String outCharset;

	private boolean onoff = true;

	private CookiesManager cookiesManager;

	private static Set<String> keyNames = new HashSet<String>();

	private String metaCookieName;

	private String metaDomain;

	private Encode encode;

	static {
		keyNames.add(CREATION_TIME);
		keyNames.add(SESSION_ID);
		keyNames.add(LAST_ACCESSED_TIME);
	}

	public void setMetaDomain(String metaDomain) {
		this.metaDomain = metaDomain;
	}

	public void setMetaCookieName(String metaCookieName) {
		this.metaCookieName = metaCookieName;
	}

	public void setEncode(Encode encode) {
		this.encode = encode;
	}

	public void setCookiesManager(CookiesManager cookiesManager) {
		this.cookiesManager = cookiesManager;
	}

	public void setOnoff(boolean onoff) {
		this.onoff = onoff;
	}

	public void setOutCharset(String outCharset) {
		this.outCharset = outCharset;
	}

	@Override
	public void initFilterBean() throws ServletException {
		super.initFilterBean();
		log.info("Nosession filter init...");
		List<AttributeCookieStore> list1 = objectFactory
				.getBeansOfType4List(AttributeCookieStore.class);
		if (ArrayUtil.isEmpty(list1) == false) {
			attributeCookieStores.addAll(list1);
		}
		List<AttributeStore> list2 = objectFactory
				.getBeansOfType4List(AttributeStore.class);
		if (ArrayUtil.isEmpty(list2) == false) {
			attributeStores.addAll(list2);
		}
		for (AttributeCookieStore store : this.attributeCookieStores) {
			enumerations.addAll(store.getAttributeNames());
		}
		for (AttributeStore store : this.attributeStores) {
			enumerations.addAll(store.getAttributeNames());
		}
		StringBuffer attrs = new StringBuffer("");
		for (String key : enumerations) {
			attrs.append(key).append(",");
		}
		log.info("Nosession  registed attribute[{}]", attrs);
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public void setUuidGenerator(UUIDGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, FilterChain filterChain)
			throws ServletException, IOException {
		if (onoff) {
			final Map<String, StoreContext> attributes = new HashMap<String, StoreContext>();

			final HttpServletRequest request = new HttpServletRequestWrapper(
					servletRequest) {
				private HttpSession _httpSession;

				private HttpServletRequest innHttpServletRequest = this;

				@Override
				public Object getAttribute(String name) {
					return super.getAttribute(name);
				}

				@Override
				public HttpSession getSession() {
					return getSession(false);
				}

				final class HttpSessionInternal implements HttpSession {
					private boolean isNew = true;
					private boolean isValid = true;

					public void setId(String id) {
						this.setAttribute(SESSION_ID, id);
					}

					public long getCreationTime() {
						return getAttribute((CREATION_TIME)) == null ? 0l
								: ((Long) getAttribute((CREATION_TIME)))
										.longValue();
					}

					public String getId() {
						return (String) getAttribute(SESSION_ID);

					}

					private void checkInvalid() {
						if (false == isValid()) {
							throw new IllegalStateException(
									"melody session has been invalidated");
						}
					}

					private boolean isValid() {
						return isValid;
					}

					public long getLastAccessedTime() {
						return getAttribute((LAST_ACCESSED_TIME)) == null ? 0l
								: ((Long) getAttribute((LAST_ACCESSED_TIME)))
										.longValue();
					}

					public ServletContext getServletContext() {
						checkInvalid();
						return servletContext;
					}

					public void setMaxInactiveInterval(int interval) {

					}

					public int getMaxInactiveInterval() {
						return maxInactiveInterval;
					}

					public HttpSessionContext getSessionContext() {
						throw new UnsupportedOperationException(
								"No longer supported method: getSessionContext");
					}

					public Object getAttribute(String key) {
						if (keyNames.contains(key) == false) {
							checkInvalid();
						}
						if (attributes.containsKey(key)) {
							return attributes.get(key).getValue();
						}
						if (keyNames.contains(key)) {
							String cookieValue = cookiesManager
									.readCookieValue(innHttpServletRequest,
											metaCookieName);
							if (StringUtil.isEmpty(cookieValue) == false) {
								@SuppressWarnings("unchecked")
								Map<String, Object> map = (Map<String, Object>) encode
										.decode(cookieValue);
								if (map != null) {
									for (Entry<String, Object> entry : map
											.entrySet()) {
										attributes.put(
												entry.getKey(),
												new StoreContext(entry
														.getValue()));
									}
								}
							}
							return attributes.containsKey(key) ? attributes
									.get(key).getValue() : null;
						}
						for (AttributeCookieStore store : attributeCookieStores) {
							if (store.isMatch(key)) {
								String cookieValue = cookiesManager
										.readCookieValue(innHttpServletRequest,
												store.getCookieName());
								if (StringUtil.isEmpty(cookieValue) == false) {
									@SuppressWarnings("unchecked")
									Map<String, Object> map = (Map<String, Object>) store
											.getEncode().decode(cookieValue);
									if (map != null) {
										for (Entry<String, Object> entry : map
												.entrySet()) {
											attributes.put(
													entry.getKey(),
													new StoreContext(entry
															.getValue()));
										}
									}
								}
							}
						}
						for (AttributeStore store : attributeStores) {
							if (store.isMatch(key)) {
								Map<String, Object> map = store.loadValue();
								if (map != null) {
									for (Entry<String, Object> entry : map
											.entrySet()) {
										attributes.put(
												entry.getKey(),
												new StoreContext(entry
														.getValue()));
									}
								}
							}
						}
						return attributes.containsKey(key) ? attributes
								.get(key).getValue() : null;
					}

					public Object getValue(String name) {
						checkInvalid();
						return getAttribute(name);
					}

					@SuppressWarnings({ "rawtypes" })
					public Enumeration getAttributeNames() {
						return Collections.enumeration(enumerations);
					}

					public String[] getValueNames() {
						return (String[]) enumerations.toArray();
					}

					public void setAttribute(String key, Object value) {
						checkInvalid();
						if (keyNames.contains(key)) {
							if (attributes.containsKey(key)) {
								attributes.get(key).setValue(value);
							} else {
								attributes.put(key, new StoreContext(value,
										true));
							}
						}
						for (AttributeCookieStore store : attributeCookieStores) {
							if (store.isMatch(key)) {
								enumerations.add(key);
								if (attributes.containsKey(key)) {
									attributes.get(key).setValue(value);
								} else {
									attributes.put(key, new StoreContext(value,
											true));
								}
							}
						}
						for (AttributeStore store : attributeStores) {
							if (store.isMatch(key)) {
								enumerations.add(key);
								if (attributes.containsKey(key)) {
									attributes.get(key).setValue(value);
								} else {
									attributes.put(key, new StoreContext(value,
											true));
								}
							}
						}
					}

					public void putValue(String name, Object value) {
						setAttribute(name, value);
					}

					public void removeAttribute(String key) {
						checkInvalid();
						setAttribute(key, null);
					}

					public void removeValue(String name) {
						removeAttribute(name);
					}

					public void invalidate() {
						this.isValid = false;
						Iterator<String> it = attributes.keySet().iterator();
						while (it.hasNext()) {
							String key = it.next();
							if (keyNames.contains(key) == false) {
								attributes.get(key).setValue(null);
								enumerations.remove(key);
							}
						}
						for (AttributeStore store : attributeStores) {
							store.invalidate();
						}
					}

					public boolean isNew() {
						return this.isNew;
					}

					/**
					 * 
					 * @return
					 */
					private boolean timeOut() {
						long nowTime = System.currentTimeMillis();
						if (maxInactiveInterval != -1
								&& nowTime > (getLastAccessedTime() + maxInactiveInterval * 1000)) {
							return true;
						}
						return false;
					}
				}

				@Override
				public HttpSession getSession(boolean create) {
					if (_httpSession != null) {
						return _httpSession;
					}
					HttpSessionInternal httpSessionInternal = new HttpSessionInternal();
					_httpSession = httpSessionInternal;
					String jesssionid = httpSessionInternal.getId();
					if (StringUtil.isEmpty(jesssionid) == true) {
						httpSessionInternal.setId(uuidGenerator.gain());
						httpSessionInternal.setAttribute(CREATION_TIME,
								System.currentTimeMillis());
					} else {
						httpSessionInternal.isNew = false;
					}

					if (false == httpSessionInternal.isNew) {
						if (httpSessionInternal.timeOut()) {
							if (log.isDebugEnabled()) {
								log.debug("the session is time out! id ="
										+ httpSessionInternal.getId());
							}
							if (create == true) {
								httpSessionInternal.setId(uuidGenerator.gain());
								httpSessionInternal.setAttribute(CREATION_TIME,
										System.currentTimeMillis());
								return httpSessionInternal;
							} else {
								return null;
							}
						}
					}
					httpSessionInternal.setAttribute(LAST_ACCESSED_TIME,
							System.currentTimeMillis());
					return httpSessionInternal;
				}
			};

			HttpServletResponse response = new HttpServletResponseWrapper(
					servletResponse) {

				private boolean _commited = false;

				final private OutoutWrapper _outoutWrapper = new OutoutWrapper();

				@Override
				public void addCookie(Cookie cookie) {
					if (true == isCommitted() || _commited == true) {
						return;
					}
					Iterator<Cookie> it = _cookies.iterator();
					while (it.hasNext() == true) {
						Cookie cookieTemp = it.next();
						if (cookieTemp.getName().equals(cookie.getName())) {
							_cookies.remove(cookieTemp);
							it = _cookies.iterator();
						}
					}
					_cookies.add(cookie);
				}

				private void cookiesCommit() {
					if (_commited == false) {
						if (true == isCommitted()) {
							return;
						}
						if (request.getSession() != null) {
							Map<String, Object> map = new HashMap<String, Object>();
							for (String key : keyNames) {
								map.put(key, attributes.get(key).getValue());
							}
							if (attributes.get(SESSION_ID).isModified()
									|| attributes.get(CREATION_TIME)
											.isModified()
									|| maxInactiveInterval != -1) {
								try {
									cookiesManager
											.writeCookie(
													request,
													this,
													new com.hundsun.jresplus.web.nosession.cookie.Cookie(
															metaCookieName,
															encode.encode(map),
															true,
															maxInactiveInterval,
															"/", metaDomain));
								} catch (SerializationException e1) {
									throw new RuntimeException(e1);
								} catch (SessionEncoderException e1) {
									throw new RuntimeException(e1);
								}
							}
							for (AttributeCookieStore attributeStore : attributeCookieStores) {
								Map<String, Object> tmp = new HashMap<String, Object>();
								boolean allNotModified = true;
								for (Entry<String, StoreContext> entry : attributes
										.entrySet()) {
									if (attributeStore.isMatch(entry.getKey())) {
										if (entry.getValue().isModified()) {
											allNotModified = false;
										}
										if (entry.getValue().getValue() != null) {
											tmp.put(entry.getKey(), entry
													.getValue().getValue());
										}
									}
								}
								try {
									if (attributeStore.getMaxInactiveInterval() == -1
											&& allNotModified == true) {
										continue;
									}
									if (tmp.size() > 0) {
										cookiesManager
												.writeCookie(
														request,
														this,
														new com.hundsun.jresplus.web.nosession.cookie.Cookie(
																attributeStore
																		.getCookieName(),
																attributeStore
																		.getEncode()
																		.encode(tmp),
																true,
																attributeStore
																		.getMaxInactiveInterval(),
																attributeStore
																		.getPath(),
																attributeStore
																		.getDomain()));
									} else {
										cookiesManager
												.writeCookie(
														request,
														this,
														new com.hundsun.jresplus.web.nosession.cookie.Cookie(
																attributeStore
																		.getCookieName(),
																null,
																true,
																0,
																attributeStore
																		.getPath(),
																attributeStore
																		.getDomain()));
									}
								} catch (SessionEncoderException e) {
									log.error(
											"load meta data from cookie error !",
											e);
								} catch (SerializationException e) {
									log.error(
											"load meta data from cookie error !",
											e);
								}
							}
						}
						for (Cookie cookie : _cookies) {
							if (cookie instanceof com.hundsun.jresplus.web.nosession.cookie.Cookie) {
								addHeader(
										getCookieHeaderName(cookie),
										getCookieHeaderValue((com.hundsun.jresplus.web.nosession.cookie.Cookie) cookie));
							} else {
								super.addCookie(cookie);
							}
						}
						_commited = true;
					}
				}

				@Override
				public void flushBuffer() throws IOException {
					try {
						cookiesCommit();
						ServletOutputStream sos = super.getOutputStream();
						_outoutWrapper.getPw().flush();
						_outoutWrapper.getBos().writeTo(sos);
						sos.flush();
					} finally {
						_outoutWrapper.reset();
					}
				}

				@Override
				public void reset() {
					_outoutWrapper.reset();
				}

				@Override
				public void resetBuffer() {
					_outoutWrapper.reset();
				}

				@Override
				public int getBufferSize() {
					return Integer.MAX_VALUE;
				}

				private String getCookieHeaderName(Cookie cookie) {
					return ServerCookie
							.getCookieHeaderName(cookie.getVersion());
				}

				private String getCookieHeaderValue(
						com.hundsun.jresplus.web.nosession.cookie.Cookie cookie)
						throws IllegalArgumentException {
					return appendCookieHeaderValue(new StringBuilder(), cookie)
							.toString();
				}

				private StringBuilder appendCookieHeaderValue(
						StringBuilder buf,
						com.hundsun.jresplus.web.nosession.cookie.Cookie cookie)
						throws IllegalArgumentException {
					ServerCookie.appendCookieValue(buf, cookie.getVersion(),
							cookie.getName(), cookie.getValue(),
							cookie.getPath(), cookie.getDomain(),
							cookie.getComment(), cookie.getMaxAge(),
							cookie.getSecure(), cookie.isHttpOnly());
					return buf;
				}

				private Set<Cookie> _cookies = new HashSet<Cookie>();

				@Override
				public void sendRedirect(String location) throws IOException {
					cookiesCommit();
					super.sendRedirect(location);
				}

				@Override
				public ServletOutputStream getOutputStream() throws IOException {
					return _outoutWrapper.getServletOutputStream();
				}

				@Override
				public PrintWriter getWriter() throws IOException {
					return _outoutWrapper.getPw();
				}

				@Override
				public void setBufferSize(int size) {
				}

				final class OutoutWrapper {

					private BufferedByteArrayOutputStream bos;

					private PrintWriter pw;

					public OutoutWrapper() throws IOException {
						super();
						this.bos = outputStreams.get();
						if (bos == null) {
							this.bos = new BufferedByteArrayOutputStream(
									outBufferSize);
							outputStreams.set(this.bos);
						}
						this.pw = new PrintWriter(new OutputStreamWriter(bos,
								outCharset));
					}

					public BufferedByteArrayOutputStream getBos() {
						return bos;
					}

					public PrintWriter getPw() {
						return pw;
					}

					public void reset() {
						bos.reset(recyclingBufferBlockSize);
					}

					public ServletOutputStream getServletOutputStream() {
						return new ServletOutputStream() {

							@Override
							public void write(int b) throws IOException {
								bos.write(b);
							}

							@Override
							public void write(byte[] b, int off, int len)
									throws IOException {
								bos.write(b, off, len);
							}

							@Override
							public void write(byte[] b) throws IOException {
								bos.write(b);
							}

							@Override
							public void close() throws IOException {
							}

							@Override
							public void flush() throws IOException {
							}
						};
					}
				}
			};

			boolean isFlush = true;
			try {
				filterChain.doFilter(request, response);
			} catch (Throwable e) {
				isFlush = false;
				throw new ServletException(e);
			} finally {
				if (request.getSession() != null) {
					for (AttributeStore attributeStore : attributeStores) {
						attributeStore.setValue(attributes);
					}
				}
				if (isFlush) {
					response.flushBuffer();
				} else {
					response.reset();
				}
				attributes.clear();
			}
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
}
