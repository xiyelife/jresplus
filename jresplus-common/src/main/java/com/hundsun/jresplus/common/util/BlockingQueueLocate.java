package com.hundsun.jresplus.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingQueueLocate {
	private static final Logger logger = LoggerFactory
			.getLogger(BlockingQueueLocate.class);

	private static final String Jdk7LinkedTransferQueue = "java.util.concurrent.LinkedTransferQueue";

	private static final String Jsr166yLinkedTransferQueue = "jsr166y.LinkedTransferQueue";

	private static Class<?> queueClass = findQueueClass();

	private static Class<?> findQueueClass() {
		Class<?> clazz;
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			clazz = classLoader.loadClass(Jdk7LinkedTransferQueue);
			if (logger.isDebugEnabled()) {
				logger.debug("use " + Jdk7LinkedTransferQueue);
			}
			return clazz;
		} catch (ClassNotFoundException ignore) {
			if (logger.isDebugEnabled()) {
				logger.debug(Jdk7LinkedTransferQueue + " not find.");
			}
		}
		try {
			clazz = classLoader.loadClass(Jsr166yLinkedTransferQueue);
			if (logger.isDebugEnabled()) {
				logger.debug("use " + Jsr166yLinkedTransferQueue);
			}
			return clazz;
		} catch (ClassNotFoundException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(Jsr166yLinkedTransferQueue + " not find.");
			}
		}
		// ConcurrentLinkedQueue从jdk1.5开始就有了
		if (logger.isDebugEnabled()) {
			logger.debug("use " + ConcurrentLinkedQueue.class);
		}
		return LinkedBlockingQueue.class;
	}

	@SuppressWarnings("unchecked")
	public static <T> BlockingQueue<T> createBlockingQueue() {
		try {
			return (BlockingQueue<T>) queueClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
