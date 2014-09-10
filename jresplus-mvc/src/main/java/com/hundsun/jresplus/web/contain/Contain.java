package com.hundsun.jresplus.web.contain;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.Renderable;

import com.hundsun.jresplus.web.velocity.eventhandler.DirectOutput;

/**
 * contain可以使用toolbox配置,也可使用containFilter配置在spring中
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */

public class Contain {
	private static final Log log = LogFactory.getLog(Contain.class);

	/**
	 * 保存在request中,contain被调用的标记key,值为当前cotain被调用的深度
	 */
	public static final String ContainCounterKey = "_iContain";

	private HttpServletRequest request;

	private HttpServletResponse response;

	private static final int recursiveLevel = 20;

	private static final ThreadLocal<VaryInt> count = new ThreadLocal<VaryInt>() {

		@Override
		protected VaryInt initialValue() {
			return new VaryInt();
		}

	};

	public Contain() {
		super();
	}

	public Contain(HttpServletRequest request, HttpServletResponse response) {
		super();
		this.request = request;
		this.response = response;
	}

	public ControlRender setTemplate(String controlName) {
		if (controlName == null) {
			throw new NullPointerException(
					"control template name can't be null.");
		}
		return new ControlRender(controlName);
	}

	public ControlRender get(String controlName) {
		return setTemplate(controlName);
	}

	public class ControlRender implements DirectOutput, Renderable {

		private String controlName;

		private Set<ContainParameter> parameters = new HashSet<ContainParameter>();

		public ControlRender(String controlName) {
			this.controlName = controlName;
		}

		public ControlRender put(String key, Object value) {
			parameters.add(new ContainParameter(Contain.this.request, key,
					value));
			return this;
		}

		public ControlRender setParameter(String key, Object value) {
			return this.put(key, value);
		}

		public boolean render(InternalContextAdapter adapter, Writer writer)
				throws IOException, MethodInvocationException,
				ParseErrorException, ResourceNotFoundException {
			if (enter()) {
				log.error("contain recursive invoked,so exist.");
				return true;
			}
			parameters.add(new ContainParameter(Contain.this.request,
					ContainCounterKey, count.get().i));
			ResponseContainWrapper wrapper = new ResponseContainWrapper(
					Contain.this.response, writer);
			try {
				Contain.this.request.getRequestDispatcher(controlName).include(
						Contain.this.request, wrapper);
				if (this.parameters != null) {
					for (ContainParameter cp : this.parameters) {
						cp.recover(Contain.this.request);
					}
				}
			} catch (ServletException e) {
				if (log.isErrorEnabled()) {
					log.error("error in control render.", e);
				}
				writer.write(e.getMessage());
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("error in control render.", e);
				}
				writer.write(e.getMessage());
			} finally {
				leave();
			}
			return true;
		}

		@Override
		public String toString() {
			if (enter()) {
				log.error("contain recursive invoked,so exist.");
				return "";
			}
			parameters.add(new ContainParameter(Contain.this.request,
					ContainCounterKey, count.get().i));
			ResponseStringWriterWrapper responseWrapper = new ResponseStringWriterWrapper(
					Contain.this.response);
			try {
				Contain.this.request.getRequestDispatcher(controlName).include(
						Contain.this.request, responseWrapper);
				String back = responseWrapper.getStringWriter().toString();
				if (this.parameters != null) {
					for (ContainParameter cp : this.parameters) {
						cp.recover(Contain.this.request);
					}
				}
				return back;
			} catch (ServletException e) {
				if (log.isErrorEnabled()) {
					log.error("error in control render.", e);
				}
				return e.getMessage();
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("error in control render.", e);
				}
				return e.getMessage();
			} finally {
				leave();
			}
		}

		/**
		 * 调用了一次,返回是否超过递归层次
		 * 
		 * @return
		 */
		private boolean enter() {
			VaryInt vi = count.get();
			int now = vi.add();
			if (now >= recursiveLevel) {
				vi.i = 0;
				return true;
			}
			return false;
		}

		private void leave() {
			VaryInt vi = count.get();
			vi.sub();
		}
	}

	private static class ContainParameter {
		private String key;
		private Object original;

		public ContainParameter(HttpServletRequest request, String key,
				Object newValue) {
			this.key = key;
			this.original = request.getAttribute(key);
			request.setAttribute(key, newValue);
		}

		public void recover(HttpServletRequest request) {
			request.setAttribute(this.key, this.original);
		}
	}

	private static final class VaryInt {
		int i = 0;

		public int add() {
			i++;
			return i;
		}

		public int sub() {
			i--;
			return i;
		}
	}

	public static boolean isInContain(HttpServletRequest request) {
		// 如果是在执行一个contain渲染，则无需layout,执行普通渲染逻辑即可
		Integer cCount = (Integer) request
				.getAttribute(Contain.ContainCounterKey);
		if (cCount != null && cCount > 0) {
			return true;
		}

		return false;
	}

	public static boolean isInContain(Context context) {
		// 如果是在执行一个contain渲染，则无需layout,执行普通渲染逻辑即可
		Integer cCount = (Integer) context.get(Contain.ContainCounterKey);
		if (cCount != null && cCount > 0) {
			return true;
		}

		return false;
	}
}
