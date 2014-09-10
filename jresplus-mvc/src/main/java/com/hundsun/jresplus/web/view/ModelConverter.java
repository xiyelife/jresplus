package com.hundsun.jresplus.web.view;

import java.util.Map;

/**
 * 
 * 功能说明: 用于FastJsonView,MarshallingView的Model转换器 开发人员: XIE <br>
 * 开发时间: 2014-8-8 <br>
 * 功能描述: json视图和xml视图的Model数据进行转换处理，可以转换为统一的模型方便业务定制<br>
 */
public interface ModelConverter {

	public Model convert(Map model);

	public Model convert(Object obj);
}
