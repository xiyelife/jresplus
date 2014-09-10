package com.hundsun.jresplus.web.contain.pipeline;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Renderable;
/**
 * Bigpipe渲染任务，一个bp输出会使用一个PipelineTask封装放置到队列中等待PipelineExport的触发
 * @author XIE (xjj@hundsun.com)
 *
 */
public class PipelineTask implements Renderable {
	public static final String ATTR_KEY = "asyncContainContext";
	public static final int DEFAULT_QUEUE_SIZE = 8;
	public static final Comparator<PipelineTask> COMPARATOR = new PipelineTaskComparator();
	private int order = Integer.MAX_VALUE;
	private String view;
	private String id;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private Set<String> js;
	private StringBuilder jsCode = new StringBuilder();
	private Set<String> css;
	private boolean hasCss = false;
	private Queue<PipelineTask> subTask = new PriorityQueue<PipelineTask>(
			DEFAULT_QUEUE_SIZE, COMPARATOR);
	private PipelineTask parentTask;

	public boolean render(InternalContextAdapter context, Writer writer)
			throws IOException, MethodInvocationException, ParseErrorException,
			ResourceNotFoundException {
		writer.write("<div id=\"" + id + "\"></div>");
		return true;
	}

	public void addJsCode(String jsCode) throws IOException {
		if (StringUtils.isBlank(jsCode)) {
			return;
		}
		StringWriter sw = new StringWriter();
		StringEscapeUtils.escapeJavaScript(sw, jsCode);
		this.jsCode.append(sw);
	}

	public void setParentTask(PipelineTask task) {
		this.parentTask = task;
	}

	public void addSubTask(PipelineTask task) {
		if (task != null) {
			task.setParentTask(this);
			subTask.add(task);
		}

	}

	public boolean isParentView(String view) {
		if (parentTask != null) {
			String parentView = parentTask.getView();
			if (parentView.indexOf("?") > 0) {
				parentView = parentView.substring(0, parentView.indexOf("?"));
			}
			String currentView = view;
			if (view.indexOf("?") > 0) {
				currentView = view.substring(0, view.indexOf("?"));
			}
			if (parentView.equals(currentView)) {
				return true;
			}
			return parentTask.isParentView(currentView);
		}
		return false;
	}

	public boolean isHasSubTask() {
		return subTask.size() > 0;
	}

	public Queue<PipelineTask> getSubTask() {
		return this.subTask;
	}

	public void addJs(String js) {
		if (StringUtils.isBlank(js)) {
			return;
		}
		if (this.js == null) {
			this.js = new HashSet<String>();
		}
		this.js.add(js);
	}

	public void addCss(String css) {
		if (StringUtils.isBlank(css)) {
			return;
		}
		if (this.css == null) {
			this.css = new HashSet<String>();
		}
		this.css.add(css);
		this.hasCss = true;
	}

	public boolean isHasCss() {
		return hasCss;
	}

	public Set<String> getJs() {
		return js;
	}

	public StringBuilder getJsCode() {
		return jsCode;
	}

	public Set<String> getCss() {
		return css;
	}

	public PipelineTask(String view, int order) {
		this.view = view;
		this.order = order;
	}

	public PipelineTask put(String key, Object value) {
		parameters.put(key, value);
		return this;
	}

	public PipelineTask setParameter(String key, Object value) {
		return this.put(key, value);
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public PipelineTask(String view, String id) {
		this.view = view;
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
