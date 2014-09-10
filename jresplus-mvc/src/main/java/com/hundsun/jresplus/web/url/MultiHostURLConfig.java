package com.hundsun.jresplus.web.url;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class MultiHostURLConfig extends URLConfig {

	/**
	 */
	private String[] hosts;

	/**
	 */
	private int hostsCount;

	/**
	 * @param id
	 * @return
	 */
	public String getURL(int id) {
		StringBuffer sb = new StringBuffer();
		if (StringUtil.isNotBlank(protocol)) {
			sb.append(protocol).append("://");
		}
		String _host = getCurrnetHost(id);
		if (StringUtil.isBlank(_host)) {
			return "";
		}
		sb.append(getCurrnetHost(id));
		if (port != null) {
			if ((protocol.equals("http") && port == 80)
					|| (protocol.equals("https") && port == 445)) {

			} else {
				sb.append(":").append(port);
			}
		}
		if (sb.charAt(sb.length() - 1) == '/') {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	private String getCurrnetHost(int id) {
		if (hostsCount == 1) {
			return hosts[0].trim();
		}
		if (hostsCount == 0) {
			return "";
		}
		int remainder = Math.abs(id) % hostsCount;
		return hosts[remainder].trim();
	}

	public void setHostsValues(String s) {

		setHosts(s.split(","));
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
		this.hostsCount = hosts.length;
	}

	@Override
	public void setHost(String host) {
		throw new IllegalArgumentException(
				"MultiHostURLConfig not support host property.");
	}

}
