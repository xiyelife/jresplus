/*
 * 系统名称: JRES 应用快速开发企业套件
 * 模块名称: JRES内核
 * 文件名称: URLConfig.java
 * 软件版权: 恒生电子股份有限公司
 * 修改记录:
 * 修改日期            修改人员                     修改说明 <br>
 * ========    =======  ============================================
 * 20140210		hanyin	STORY #7560 -URLBroker支持动态IP和端口支持
 * ========    =======  ============================================
 */

package com.hundsun.jresplus.web.url;

import org.apache.commons.lang.StringUtils;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * @author eyeieye
 * 
 */

public class URLConfig {

	protected String protocol = "http";

	protected String host;

	protected Integer port = 80;

	protected String path;

	protected String buildURL = null;

	protected boolean followContextPath = false;

	/**
	 * 
	 * @return
	 */
	public String getURL() {
		if (buildURL == null) {
			// { 20140210 begin add hanyin STORY #7560
			// 如果没有配置host，或者配置为空串，则采用相对路径寻址
			if (StringUtil.isBlank(host)) {
				return "";
			}
			// } 20140210 end add hanyin STORY #7560

			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotBlank(protocol)) {
				sb.append(protocol).append("://");
			}
			if (StringUtils.isNotBlank(host)) {
				sb.append(host);
			}
			if (port != null) {
				if ((protocol.equals("http") && port == 80)
						|| (protocol.equals("https") && port == 443)) {

				} else {
					sb.append(":").append(port);
				}
			}
			if (sb.charAt(sb.length() - 1) == '/') {
				sb.deleteCharAt(sb.length() - 1);
			}
			buildURL = sb.toString();
		}
		return buildURL;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host.toLowerCase();
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol.toLowerCase();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFollowContextPath(boolean followContextPath) {
		this.followContextPath = followContextPath;
	}

	public boolean isFollowContextPath() {
		return followContextPath;
	}

}
