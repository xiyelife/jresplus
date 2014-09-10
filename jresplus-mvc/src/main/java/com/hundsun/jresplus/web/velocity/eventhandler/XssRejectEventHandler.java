package com.hundsun.jresplus.web.velocity.eventhandler;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能说明: 防止跨站点脚本注入的处理器，该处理器配置后可以对视图开发中的引用变量在输出前进行转码处理，防止恶意的脚本注入导致浏览器崩溃和数据泄露.
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-22 <br>
 * 功能描述: 使用该处理器会对视图的渲染有一定的开销，如果只是想手工进行显示的处理，可以尝试使用配置org.apache.commons.lang.
 * StringEscapeUtils到vm-toolbox.xml的方式<br>
 * 如果想要统一处理，可以配置该类到参数配置文件中<br/>
 * eventhandler.referenceinsertion.class=com.hundsun.jresplus.web.velocity.
 * eventhandler.XssRejectEventHandler<br/>
 */
public class XssRejectEventHandler implements ReferenceInsertionEventHandler,
		RuntimeServicesAware {

	private static final Logger logger = LoggerFactory
			.getLogger(XssRejectEventHandler.class);
	private static final String DirectOutputVariableConfiguration = "reference.insertion.event.handler.direct.variable.names";

	private Set<String> directOutputVariables = new HashSet<String>();

	public Object referenceInsert(String reference, Object value) {
		if (value == null) {
			return value;
		}
		if (directOutputVariables.contains(reference)) {
			return value;
		}
		if (value instanceof String) {
			return StringEscapeUtils.escapeHtml((String) value);
		}
		return value;
	}

	public void setRuntimeServices(RuntimeServices rs) {
		String[] temp = rs.getConfiguration().getStringArray(
				DirectOutputVariableConfiguration);
		if (temp != null && temp.length > 0) {
			for (String s : temp) {
				if (StringUtils.isNotBlank(s)) {
					directOutputVariables.add(s.trim());
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("init DirectOutputVariable with:"
					+ directOutputVariables);
		}
	}
}
