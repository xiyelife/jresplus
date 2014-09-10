package com.hundsun.jresplus.web.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hundsun.jresplus.exception.BaseException;
/**
 * 简单模型转换器，将springmvc返回的模型数据封装到参考模型对象中（支持返回码、错误号、错误信息）
 * @author XIE (xjj@hundsun.com)
 *
 */
public class SimpleModelConverter implements ModelConverter {

	private String successRetCode = "0";
	private String failRetCode = "-1";

	public Model convert(Map model) {
		if (model == null) {
			return null;
		}
		Model mod = new Model();
		mod.setRetCode(successRetCode);
		Set<Entry> entrySet = model.entrySet();
		int size = model.size();
		if (size > 1) {
			mod.setData(new HashMap());
		}
		for (Entry entry : entrySet) {
			Object value = entry.getValue();
			Object key = entry.getKey();

			if (value instanceof Exception) {
				Exception ex = (Exception) value;
				processException(mod, ex);
				continue;
			}
			if (size > 1) {
				Map dataMap = (Map) mod.getData();
				dataMap.put(key, value);
				continue;
			}
			mod.setData(value);

		}
		return mod;
	}

	public Model convert(Object model) {
		if (model == null) {
			return null;
		}
		Model mod = new Model();
		if (model instanceof Exception) {
			processException(mod, (Exception) model);
		} else {
			mod.setRetCode(successRetCode);
			mod.setData(model);
		}
		return mod;
	}

	private void processException(Model mod, Exception ex) {
		mod.setErrInfo(ex.getMessage());
		mod.setRetCode(this.failRetCode);
		if (ex instanceof BaseException) {
			BaseException bex = (BaseException) ex;
			mod.setErrCode(bex.getErrorCode());
			mod.setErrInfo(bex.getErrorMessage());
		}
	}

	public String getSuccessRetCode() {
		return successRetCode;
	}

	public void setSuccessRetCode(String successRetCode) {
		this.successRetCode = successRetCode;
	}

	public String getFailRetCode() {
		return failRetCode;
	}

	public void setFailRetCode(String failRetCode) {
		this.failRetCode = failRetCode;
	}

}
