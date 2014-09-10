package com.hundsun.jresplus.web.cache.adapter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.web.cache.PageImage;
import com.hundsun.jresplus.web.cache.PageStaticCache;

/**
 * 
 * 功能说明:
 * 支持Hash策略的页面静态化缓存门面，可以使用该门面同时注入多个页面静态缓存（PageStaticCache）实现，运行时按照哈希策略路由读写.
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-22 <br>
 * 功能描述: 如果使用该类做为页面静态缓存的门面，需要将此类声明为springbean，如：<br/>
 * 
 * <pre>
 * &lt;bean id="pageStaticCache"
 * class="com.hundsun.jresplus.web.cache.HashSupportedPageStaticCacheFacade"&gt;
 * 	&lt;property name="caches"&gt; 
 * 		&lt;array&gt; 
 * 			&lt;ref bean=""/&gt; 
 * 		&lt;/array&gt; 
 * 	&lt;/property&gt; 
 * &lt;/bean&gt;
 * </pre>
 */
public class HashSupportedPageStaticCacheFacade implements PageStaticCache,
		InitializingBean {
	PageStaticCache[] caches;

	public void put(String key, PageImage content) {
		PageStaticCache cache = getCacahe(key);
		if (cache == null) {
			return;
		}
		cache.put(key, content);
	}

	public PageImage get(String key) {
		PageStaticCache cache = getCacahe(key);
		if (cache == null) {
			return null;
		}
		return cache.get(key);
	}

	private PageStaticCache getCacahe(String key) {
		if (StringUtil.isBlank(key) || caches == null) {
			return null;
		}
		int count = caches.length;
		if (count < 1) {
			return null;
		}
		int hash = key.hashCode();

		int index = Math.abs(hash) % count;
		if (index >= count) {
			index = count - 1;
		}
		return caches[index];
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(caches,
				"Use HashSupportedPageStaticCacheFacade must be inject PageStaticCache array.");
	}

	public PageStaticCache[] getCaches() {
		return caches;
	}

	public void setCaches(PageStaticCache[] caches) {
		this.caches = caches;
	}

}
