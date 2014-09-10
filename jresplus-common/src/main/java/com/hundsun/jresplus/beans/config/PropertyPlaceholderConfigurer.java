/*
 * 修改记录
 * 2013-10-31 参数文件支持多个配置
 */
package com.hundsun.jresplus.beans.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import com.hundsun.jresplus.common.util.StringUtil;

/**
 * 
 * @author LeoHu copy sagahl
 * 
 */
public class PropertyPlaceholderConfigurer extends
		org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
		implements ResourceLoaderAware {
	private static Logger logger = LoggerFactory
			.getLogger(PropertyPlaceholderConfigurer.class);
	private static Set<Object> propKeySet = new HashSet<Object>();
	public static String CONFIG_LOCATION = "JresConfigLocation";

	protected void loadProperties(Properties properties) throws IOException {
		super.loadProperties(properties);
		if (properties.containsKey("system.dev.mode")
				&& "true".equals(properties.get("system.dev.mode"))) {
			properties.put("negate.system.dev.mode", "false");
		} else {
			properties.put("negate.system.dev.mode", "true");
		}
		logLoadedProperties(properties);
	}

	private void logLoadedProperties(Properties properties) {
		Set<Object> keys = properties.keySet();
		Properties props = new Properties();
		for (Object key : keys) {
			if (propKeySet.contains(key)) {
				continue;
			}
			props.put(key, properties.get(key));
			propKeySet.add(key);
		}

		logger.info("Loaded properties:{}", props);
	}

	public Properties mergeProperties() throws IOException {
		return super.mergeProperties();
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		String config = System.getProperty(CONFIG_LOCATION);
		String[] configs = null;
		if (StringUtil.isNotBlank(config)) {
			configs = StringUtils.tokenizeToStringArray(config, ",; \t\n");
		}
		if (configs != null) {
			Resource[] resources = new Resource[configs.length];
			for (int n = 0; n < resources.length; n++) {
				resources[n] = resourceLoader.getResource(configs[n]);
			}
			super.setLocations(resources);
		} else {
			super.setLocation(resourceLoader.getResource("classpath:"
					+ System.getProperty(CONFIG_LOCATION)));
		}

	}
}
