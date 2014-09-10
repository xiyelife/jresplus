/*
 * 修改记录
 * -----------------------------------------------------------
 * 2013-7-12 XIE 增加isEmpty方法判断是否有未完成的任务，解决缺陷【BUG #5306】
 * 2014-8-12 XIE 重构Bigpipe实现为支持优先级的线性队列处理
 */
package com.hundsun.jresplus.web.contain.async;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hundsun.jresplus.common.util.StringUtil;
import com.hundsun.jresplus.web.contain.pipeline.PipelineExporter;
import com.hundsun.jresplus.web.contain.pipeline.PipelineTask;
import com.hundsun.jresplus.web.nosession.RandomShortUUID;

/**
 * 异步contain实现
 * 
 * @author LeoHu copy by sagahl fish
 * 
 */
public class AsynchronousContain {
	private static final Log logger = LogFactory
			.getLog(AsynchronousContain.class);

	public static final String executorName = "asynchronContainExecute";

	private static final ThreadLocal<String> asyncContext = new ThreadLocal<String>();

	public AsynchronousContain() {
		super();
	}

	public AsynchronousContain(HttpServletRequest request,
			HttpServletResponse response) {
		super();
		count = 100;
		exporter = new PipelineExporter(request, response);
	}

	public static boolean isAsyncConext() {
		return asyncContext.get() != null;
	}

	public static void setAsyncConext() {
		asyncContext.set(executorName);
	}

	public static void removeAsyncConext() {
		asyncContext.remove();
	}

	private PipelineExporter exporter;
	private int count = 100;

	private synchronized int nextOrder() {
		return count++;
	}

	public PipelineTask get(String view) {
		return get(view, nextOrder());
	}

	public PipelineTask get(String view, int order) {
		String id = RandomShortUUID.get();
		return get(view, id, order);
	}

	public PipelineTask get(String view, String id) {
		if (StringUtil.isBlank(id)) {
			return get(view);
		}
		return get(view, id, nextOrder());
	}

	public PipelineTask get(String view, String id, int order) {
		if (StringUtil.isBlank(view)) {
			return null;
		}
		if (StringUtil.isBlank(id)) {
			return get(view, order);
		}
		PipelineTask pipeTask = new PipelineTask(view, id);
		pipeTask.setOrder(order);
		exporter.addPipeTask(pipeTask);
		return pipeTask;
	}

	public PipelineExporter export() {
		return exporter;
	}

	public void finished() {
		if (exporter.allTaskFinished()) {
			exporter = null;
			return;
		}
		logger.error("All pipeline task must be exporter[cmd:$asyncContain.export() under the body tag]!");
	}
}
