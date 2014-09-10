package com.hundsun.jresplus.web.nosession.cookie;

import org.apache.commons.lang.SerializationException;

public interface Encode {

	public String encode(Object object) throws SerializationException;

	public Object decode(String str) throws SerializationException;
}
