package com.hundsun.jresplus.web.contain.pipeline;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Renderable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.hundsun.jresplus.common.util.ArrayUtil;
import com.hundsun.jresplus.web.contain.ResponseStringWriterWrapper;
import com.hundsun.jresplus.web.contain.async.AsynchronousContain;
/**
 * 管道输出器，使用到指令可触发Bigpipe渲染任务的执行
 * @author XIE (xjj@hundsun.com)
 *
 */
public class PipelineExporter implements Renderable {
	private static final Logger logger = LoggerFactory
			.getLogger(PipelineExporter.class);
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Queue<PipelineTask> taskQueue;
	private PipelineTask currentTask;
	private boolean isAbort = false;

	public PipelineExporter(HttpServletRequest request,
			HttpServletResponse response) {
		this.request = request;
		this.response = response;
		taskQueue = new PriorityQueue<PipelineTask>(
				PipelineTask.DEFAULT_QUEUE_SIZE, PipelineTask.COMPARATOR);
	}

	public boolean allTaskFinished() {
		return taskQueue == null || taskQueue.isEmpty();
	}

	public boolean render(InternalContextAdapter context, Writer writer)
			throws IOException, MethodInvocationException, ParseErrorException,
			ResourceNotFoundException {
		response.flushBuffer();
		AsynchronousContain.setAsyncConext();
		renderTaskQueue(writer, taskQueue);
		renderEnd(writer);
		AsynchronousContain.removeAsyncConext();
		return true;
	}

	private void renderTaskQueue(Writer writer, Queue<PipelineTask> taskQueue)
			throws IOException {
		while (!taskQueue.isEmpty()) {
			renderTask(writer, taskQueue.poll());
		}
	}

	private void renderEnd(Writer writer) throws IOException {
		if (isAbort) {
			return;
		}
		writer.write("<script type=\"text/javascript\">BigPipe.start();</script>");
		response.flushBuffer();

	}

	private void renderTask(Writer writer, PipelineTask task)
			throws IOException {
		currentTask = task;
		RequestDispatcher dispatcher = request.getRequestDispatcher(task
				.getView());
		PipelineRequestWrapper pipeRequest = new PipelineRequestWrapper(
				request, task.getParameters(), task.getView());
		pipeRequest.setAttribute(PipelineTask.ATTR_KEY, task);
		ResponseStringWriterWrapper pipeResponse = new ResponseStringWriterWrapper(
				response);
		try {
			dispatcher.include(pipeRequest, pipeResponse);
		} catch (Exception e) {
			pipeResponse.getStringWriter().write(e.getMessage());
		}
		String result = pipeResponse.getStringWriter().toString();
		write(writer, task, result);
		writer.flush();
		pipeResponse.flushBuffer();
		try {
			response.flushBuffer();
		} catch (Exception e) {
			String err = e.getMessage();
			if (e.getCause() != null
					&& (e.getCause() instanceof SocketException)) {
				err = e.getCause().getMessage();
			}
			logger.info(
					"Pipe queue will be abort,because task[{}] render response flush error:{}",
					task.getView(), err);
			taskQueue.clear();
			isAbort = true;
			return;
		}

		if (task.isHasSubTask()) {
			Queue<PipelineTask> subTask = task.getSubTask();
			renderTaskQueue(writer, subTask);

		}
	}

	private void write(Writer writer, PipelineTask task, String result)
			throws IOException {
		writer.write("<script type=\"text/javascript\">\r\n");
		writer.write("BigPipe.onArrive(");
		writer.write("{\"html\":\"");
		StringEscapeUtils.escapeJavaScript(writer, result);
		writer.write("\",\"id\":\"");
		writer.write(task.getId());
		writer.write("\",\"css\":");
		if (ArrayUtil.isEmpty(task.getCss())) {
			writer.write("[]");
		} else {
			writer.write(JSON.toJSONString(task.getCss()));
		}
		writer.write(",\"js\":");
		if (ArrayUtil.isEmpty(task.getJs())) {
			writer.write("[]");
		} else {
			writer.write(JSON.toJSONString(task.getJs()));
		}
		writer.write(",\"jsCode\":\"");
		if (task.getJsCode().length() < 1) {
			writer.write("");
		} else {
			writer.write(task.getJsCode().toString());
		}
		writer.write("\"}");
		writer.write(");\r\n</script>\r\n");
	}

	public void writeTo(Writer writer, String result, String id)
			throws IOException {

	}

	public void addPipeTask(PipelineTask pipeTask) {
		if (currentTask == null) {
			taskQueue.add(pipeTask);
		} else {
			if (currentTask.isParentView(pipeTask.getView())) {
				logger.warn("The pipeline view[{}] is close-loop,so ignore!",
						pipeTask.getView());
				return;
			}
			currentTask.addSubTask(pipeTask);
		}

	}
}
