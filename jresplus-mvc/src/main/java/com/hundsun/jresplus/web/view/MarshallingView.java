package com.hundsun.jresplus.web.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
/**
 * 基于org.springframework.web.servlet.view.xml.MarshallingView封装的view,支持模型转换
 * @author xjj
 *
 */
public class MarshallingView extends
		org.springframework.web.servlet.view.xml.MarshallingView {
	private ModelConverter modelConverter;

	protected void renderMergedOutputModel(Map model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		super.renderMergedOutputModel(filterModel(model), request, response);
	}

	protected Object locateToBeMarshalled(Map model) throws ServletException {
		Object obj = super.locateToBeMarshalled(model);
		if (modelConverter != null) {
			return modelConverter.convert(obj);
		}
		return obj;

	}

	private Map filterModel(Map model) {
		Map result = new HashMap(model.size());
		Set<String> renderedAttributes = model.keySet();
		Set<Entry> entrySet = model.entrySet();
		for (Entry entry : entrySet) {
			if (!(entry.getValue() instanceof BindingResult)
					&& renderedAttributes.contains(entry.getKey())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	public ModelConverter getModelConverter() {
		return modelConverter;
	}

	public void setModelConverter(ModelConverter modelConverter) {
		this.modelConverter = modelConverter;
	}

}
