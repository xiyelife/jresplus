package com.hundsun.jresplus.web.cache.adapter;

import java.util.Hashtable;

import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.web.cache.PageImage;
import com.hundsun.jresplus.web.cache.PageStaticCache;

/**
 * 
 * 功能说明: 简单的基于Hshtable的pageStaticcache实现，仅适用于测试、内容不多、不需要多实例间同步的场景，
 * 超出此适用场景时建议使用ehcache、memcached或者其他KV进行实现。
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-22 <br>
 * 功能描述: 基于Hashtable的页面静态化缓存简单实现<br>
 */
public class SimplePageStaticCacheWithHashtable implements PageStaticCache {

	private Hashtable<String, PageImage> table = new Hashtable<String, PageImage>();

	public void put(String key, PageImage content) {
		if (StringUtil.isBlank(key)) {
			return;
		}
		if (content == null) {
			table.remove(key);
			return;
		}
		table.put(key, content);
	}

	public PageImage get(String key) {
		if (StringUtil.isBlank(key)) {
			return null;
		}
		if (table.containsKey(key)) {
			return table.get(key);
		}
		return null;
	}

}
