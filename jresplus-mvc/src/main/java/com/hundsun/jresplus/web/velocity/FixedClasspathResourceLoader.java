package com.hundsun.jresplus.web.velocity;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.hundsun.jresplus.common.util.ArrayUtil;

/**
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class FixedClasspathResourceLoader extends ClasspathResourceLoader {

	private static final String IncludeKey = "include";

	private Set<String> include;

	private Set<String> match;

	@Override
	public void init(ExtendedProperties configuration) {
		String[] all = configuration.getStringArray(IncludeKey);
		include = new HashSet<String>();
		match = new HashSet<String>();
		if (all != null) {
			for (String s : all) {
				if (s.endsWith("/*")) {
					match.add(s.substring(0, s.length() - 1));
				} else {
					include.add(s);
				}
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("FixedClasspathResourceLoader : initialization complete with include:"
					+ include);
		}
	}

	@Override
	public InputStream getResourceStream(String name)
			throws ResourceNotFoundException {
		if (this.include.contains("*") || this.include.contains(name)) {
			return super.getResourceStream(name);
		} else if (ArrayUtil.isEmpty(match) == false) {
			for (String s : match) {
				if (name.startsWith(s)) {
					return super.getResourceStream(name);
				}
			}
		}
		return null;
	}

}
