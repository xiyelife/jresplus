package com.hundsun.jresplus.web.nosession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.hundsun.jresplus.common.util.BlockingQueueLocate;


/**
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public class UUIDPoolGenerator implements UUIDGenerator, InitializingBean,
		DisposableBean {

	private static final Logger logger = LoggerFactory
			.getLogger(UUIDPoolGenerator.class);

	private static final String EventStringPlaceholder = "esp";

	private Lock lock = new ReentrantLock();
	private Condition worker = lock.newCondition();

	private BlockingQueue<String> queue = BlockingQueueLocate
			.createBlockingQueue();

	private Thread producer;

	private boolean fixedLength = false;

	private int capacity = 1000;

	private int threshold = 200;

	public void afterPropertiesSet() throws Exception {
		if (threshold > capacity) {
			throw new IllegalArgumentException(
					"threshold can't great than capacity.");
		}
		producer = new Thread("UUIDGen") {
			@Override
			public void run() {
				int times = capacity / threshold;
				for (int i = 0; i < times; i++) {
					produce();
				}
				logger.debug("fill the queue with capacity:" + capacity);
				while (!Thread.interrupted()) {
					lock.lock();
					try {
						worker.await();
						produce();
					} catch (InterruptedException e) {
						break;
					} finally {
						lock.unlock();
					}
				}
			}
		};
		producer.setDaemon(true);
		producer.start();
	}

	public void destroy() throws Exception {
		producer.interrupt();
	}

	private void produce() {
		for (int i = 0; i < threshold; i++) {
			queue.add(fixedLength ? RandomShortUUID.getFixSize()
					: RandomShortUUID.get());
		}
		queue.add(EventStringPlaceholder);
	}

	private void startProduce() {
		lock.lock();
		try {
			worker.signal();
		} finally {
			lock.unlock();
		}
	}

	public String gain() {
		String one;
		try {
			one = queue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (one != EventStringPlaceholder) {
			return one;
		}
		startProduce();
		return gain();
	}
}
