package com.hundsun.jresplus.web.velocity.directive;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.hundsun.jresplus.web.contain.async.AsynchronousContain;
import com.hundsun.jresplus.web.contain.pipeline.PipelineTask;

/**
 * 
 * @author LeoHu copy sagahl
 * 
 */
public class JsCodeDirective extends Directive {
	protected final Log logger = LogFactory.getLog(this.getClass());

	/**
	 */
	@Override
	public String getName() {
		return "jscode";
	}

	/**
	 */
	@Override
	public int getType() {
		return BLOCK;
	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer,
			Node node) throws IOException, ResourceNotFoundException,
			ParseErrorException, MethodInvocationException {
		Node bodyNode = getBodyNode(context, node);
		if (AsynchronousContain.isAsyncConext()) {
			StringWriter sw = new StringWriter();
			bodyNode.render(context, sw);
			PipelineTask task = (PipelineTask) context
					.get(PipelineTask.ATTR_KEY);
			task.addJsCode(sw.toString());
		} else {
			writer.write("<script type=\"text/javascript\">\r\n");
			bodyNode.render(context, writer);
			writer.write("</script>\r\n");
		}
		return true;
	}

	protected Node getBodyNode(InternalContextAdapter context, Node node) {
		int children = node.jjtGetNumChildren();
		if (children == 1) {
			return node.jjtGetChild(0);
		} else {
			return node.jjtGetChild(children - 1);
		}
	}

}
