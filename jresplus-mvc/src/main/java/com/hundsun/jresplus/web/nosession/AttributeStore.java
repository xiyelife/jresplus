/*
 * 修改记录
 * 2013-9-29 增加cookie方式的session存储单元接口替换原来继承方式的扩展机制
 */
package com.hundsun.jresplus.web.nosession;

import java.util.Map;
import java.util.Set;

import org.springframework.core.Ordered;

public interface AttributeStore extends Ordered {

	public void invalidate();

	public Set<String> getAttributeNames();

	public boolean isMatch(String key);

	public Map<String, Object> loadValue();

	public void setValue(Map<String, StoreContext> values);
}
