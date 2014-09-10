package com.hundsun.jresplus.web.cache;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * 功能说明: 视图中用于缓存页面片段的块宏指令
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-22 <br>
 * 功能描述:
 * 视图中使用宏指令#cache(key,refresh)...#end，方式可以将指令块中的内容静态化缓存起来，缓存使用的key需要开发人员在使用是指定
 * ，页面请求的视图在渲染时，如果发现存在该指令则尝试从缓存中读取静态内容，如果没有静态内容则会在渲染完成后将内容静态化并缓存，供下次使用。<br>
 * 宏指令的第一个参数（字符串）用于描述缓存后的唯一标示，第二个参数（布尔）用于描述是否强制刷新（强制刷新等同于不从缓存中读取）<br/>
 * 使用该宏指令，必须启用页面静态过滤器（PageStaticFilter）
 */
public class PageSnippetCacheDirect extends Directive {

	public static final String DIRECT_NAME = "cache";
	private static final Logger logger = LoggerFactory
			.getLogger(PageSnippetCacheDirect.class);

	@Override
	public String getName() {
		return DIRECT_NAME;
	}

	@Override
	public int getType() {
		return BLOCK;
	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer,
			Node node) throws IOException, ResourceNotFoundException,
			ParseErrorException, MethodInvocationException {
		String key = null;
		boolean refresh = false;
		int liveTime = getTimeToLive(context);
		int dleTime = getTimeToDle(context);
		int num = node.jjtGetNumChildren();
		Node body = node.jjtGetChild(num - 1);
		if (num > 1) {
			SimpleNode keyNode = (SimpleNode) node.jjtGetChild(0);
			key = (String) keyNode.value(context);
		}
		if (num > 2) {
			SimpleNode refreshNode = (SimpleNode) node.jjtGetChild(1);
			Object refreshValue = refreshNode.value(context);
			if ("true".equals(refreshValue)) {
				refresh = true;
			}
			if (refreshValue instanceof Boolean) {
				refresh = ((Boolean) refreshValue).booleanValue();
			}
		}
		if (num > 3) {
			SimpleNode timeNode = (SimpleNode) node.jjtGetChild(2);
			Object timeValue = timeNode.value(context);
			if (timeValue instanceof Integer) {
				liveTime = (Integer) timeValue;
			}
			if (timeValue instanceof String) {
				try {
					liveTime = Integer.valueOf((String) timeValue);
				} catch (Exception e) {
					logger.error(
							"Direct cache third arg(maxliveTime) error[key={}]",
							key);
				}
			}
		}
		if (num > 4) {
			SimpleNode timeNode = (SimpleNode) node.jjtGetChild(3);
			Object timeValue = timeNode.value(context);
			if (timeValue instanceof Integer) {
				dleTime = (Integer) timeValue;
			}
			if (timeValue instanceof String) {
				try {
					dleTime = Integer.valueOf((String) timeValue);
				} catch (Exception e) {
					logger.error(
							"Direct cache third arg(maxliveTime) error[key={}]",
							key);
				}
			}
		}

		String cachedContent = "";
		PageStaticCache pageCache = getPaageStaticCache(context);
		PageImage image = pageCache == null || key == null ? null : pageCache
				.get(key);
		if (image != null) {
			cachedContent = image.getSnippet();
		}
		if (refresh || StringUtil.isBlank(cachedContent)) {
			StringWriter sw = new StringWriter();
			body.render(context, sw);
			cachedContent = sw.toString();
			cacheContent(pageCache, key, cachedContent, liveTime, dleTime);
		}
		writer.write(cachedContent);
		return true;
	}

	private int getTimeToLive(InternalContextAdapter context) {
		Object timeObj = context.get(PageStaticFilter.PAGE_STATIC_TIME_TO_LIVE);
		if (timeObj != null && timeObj instanceof Integer) {
			return (Integer) timeObj;
		}
		return PageStaticFilter.DEF_MAX_LIVE_TIME;
	}

	private int getTimeToDle(InternalContextAdapter context) {
		Object timeObj = context.get(PageStaticFilter.PAGE_STATIC_TIME_TO_DLE);
		if (timeObj != null && timeObj instanceof Integer) {
			return (Integer) timeObj;
		}
		return PageStaticFilter.DEF_MAX_DLE_TIME;
	}

	private void cacheContent(PageStaticCache pageStaticCache, String key,
			String content, int liveTime, int dleTime) {
		if (StringUtil.isBlank(key) || StringUtil.isBlank(content)
				|| pageStaticCache == null) {
			return;
		}
		PageImage image = new PageImage(content);
		image.setTimeToLiveSeconds(liveTime);
		image.setTimeToDleSeconds(dleTime);
		pageStaticCache.put(key, image);
	}

	private PageStaticCache getPaageStaticCache(InternalContextAdapter context) {
		Object cacheObj = context.get(PageStaticFilter.PAGE_STATIC_CACHE);
		if (cacheObj != null && cacheObj instanceof PageStaticCache) {
			return (PageStaticCache) cacheObj;
		}
		return null;
	}
}
