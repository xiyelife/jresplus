package com.hundsun.jresplus.web.url;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public abstract class URLBrokerInitHelper {

	protected final Logger logger = LoggerFactory
			.getLogger(URLBrokerInitHelper.class);

	protected URLBroker[] brokers;

	public URLBroker[] getBrokers() {
		return brokers;
	}

	public void setBrokers(URLBroker[] brokers) {
		this.brokers = brokers;
	}

	public void setBroker(URLBroker broker) {
		this.brokers = new URLBroker[] { broker };
	}

}
