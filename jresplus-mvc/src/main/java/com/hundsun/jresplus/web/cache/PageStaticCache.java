package com.hundsun.jresplus.web.cache;

/**
 * 
 * 功能说明: 页面静态化缓存接口
 * <p>
 * 系统版本: v1.0<br>
 * 开发人员: XIE xjj@hundsun.com <br>
 * 开发时间: 2014-7-21 <br>
 * 功能描述: 此接口用于描述页面及页面片段静态化后的缓存<br>
 */
public interface PageStaticCache {

	public void put(String key, PageImage image);

	public PageImage get(String key);
}
