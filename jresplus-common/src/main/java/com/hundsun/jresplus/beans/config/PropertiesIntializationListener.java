package com.hundsun.jresplus.beans.config;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hundsun.jresplus.common.util.StringUtil;

public class PropertiesIntializationListener implements ServletContextListener {
	private final static Logger log = LoggerFactory
			.getLogger(PropertiesIntializationListener.class);
	private static final String RENDER_FILE_OUTPUT = "UTF-8";
	private static final String RENDER_FILE_SPLIT = ",";
	private static final String VM = ".vm";
	private static final String CONFIG_RENDER_FILE_PATH = "config_render_file_path";
	private static final String VELOCITY_INPUT_ENCODING = RENDER_FILE_OUTPUT;
	private static final String VELOCITY_OUTPUT_ENCODING = RENDER_FILE_OUTPUT;

	private static final String PROPERTIES_FILE_CONFIG = "OuterPropertiesFile";
	private static final String DEFAULT_PROPERTIES_FILE_NAME = "jres.properties";
	private static final String USER_HOME = "user.home";

	public void contextInitialized(ServletContextEvent event) {
		try {

			VelocityContext context = velocityContextReady(event);
			renderClassPathResource(context);
			renderWebrootResource(event, context);

		} catch (FileNotFoundException e) {
			log.error("Init system error", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("Init system error", e);
			throw new RuntimeException(e);
		}

	}

	private void renderClassPathResource(VelocityContext context)
			throws IOException, FileNotFoundException {
		String vmPath = getClassPath();
		VelocityEngine ve = velocityEngineReady(vmPath);
		File[] templateFiles = getConfigFiles(vmPath);
		if (templateFiles == null) {
			return;
		}
		for (File templateFile : templateFiles) {
			String templateFileName = templateFile.getName();
			String targetFileName = vmPath
					+ File.separator
					+ templateFileName.substring(0,
							templateFileName.length() - 3);
			margeConfigFile(context, ve, templateFileName, targetFileName);
		}
	}

	private void renderWebrootResource(ServletContextEvent event,
			VelocityContext context) throws IOException,
			UnsupportedEncodingException, FileNotFoundException {
		String[] renders = getRenderConfigFilePath(context);
		for (String render : renders) {
			String renderFilePath = getWebRootPath(event, render);
			VelocityEngine ve = velocityEngineReady(renderFilePath);
			String targetFileName = event.getServletContext().getRealPath(
					render.substring(0, render.length() - 3));

			String templateFileName = render.substring(
					render.lastIndexOf("/") + 1, render.length());
			margeConfigFile(context, ve, templateFileName, targetFileName);
		}
	}

	private String getClassPath() {
		String vmPath = Thread.currentThread().getContextClassLoader()
				.getResource("").getFile();
		return vmPath;
	}

	private String getWebRootPath(ServletContextEvent event, String render) {
		return event.getServletContext().getRealPath(
				render.substring(0, render.lastIndexOf("/")));
	}

	private VelocityEngine velocityEngineReady(String renderFilePath) {
		VelocityEngine ve = new VelocityEngine();
		Properties properties = new Properties();
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
				renderFilePath);
		properties
				.setProperty(Velocity.INPUT_ENCODING, VELOCITY_INPUT_ENCODING);
		properties.setProperty(Velocity.OUTPUT_ENCODING,
				VELOCITY_OUTPUT_ENCODING);

		ve.init(properties);
		return ve;
	}

	private File[] getConfigFiles(String filePath) {
		File targetDir = new File(filePath);
		if (!targetDir.isDirectory()) {
			return null;
		}
		File[] targetFiles = targetDir.listFiles(new FilenameFilter() {
			public boolean accept(File file, String name) {
				return name.endsWith(VM);
			}
		});
		return targetFiles;
	}

	private String[] getRenderConfigFilePath(VelocityContext context) {
		String configToRenders = (String) context.get(CONFIG_RENDER_FILE_PATH);
		if (StringUtil.isEmpty(configToRenders)) {
			return new String[] {};
		}
		String[] renders = configToRenders.split(RENDER_FILE_SPLIT);
		return renders;
	}

	private VelocityContext velocityContextReady(ServletContextEvent event)
			throws FileNotFoundException, IOException {
		VelocityContext context = new VelocityContext();
		loadOuterProperties(event, context);
		return context;
	}

	private void margeConfigFile(VelocityContext context, VelocityEngine ve,
			String templateFileName, String targetFileName) throws IOException,
			FileNotFoundException {
		Template template = ve.getTemplate(templateFileName);
		File resultFile = new File(targetFileName);
		if (false == resultFile.exists()) {
			resultFile.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(resultFile), RENDER_FILE_OUTPUT));
		template.merge(context, writer);
		writer.flush();
		writer.close();
		log.info("Properties init marge config file[TEMPLATE:{} to {}]",
				templateFileName, targetFileName);
	}

	private void loadOuterProperties(ServletContextEvent event,
			VelocityContext context) throws FileNotFoundException, IOException {
		Properties props1 = new Properties();
		props1.put("webroot", event.getServletContext().getRealPath("")
				.replaceAll("\\\\", "/"));
		String outFIle = event.getServletContext().getInitParameter(
				PROPERTIES_FILE_CONFIG);
		if (StringUtils.isEmpty(outFIle)) {
			outFIle = DEFAULT_PROPERTIES_FILE_NAME;
		}
		InputStream in = new BufferedInputStream(new FileInputStream(
				System.getProperty(USER_HOME) + File.separator + outFIle));
		props1.load(in);
		Properties props = props1;
		Set<Entry<Object, Object>> entrySet = props.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			context.put(entry.getKey().toString(), entry.getValue());
		}
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
