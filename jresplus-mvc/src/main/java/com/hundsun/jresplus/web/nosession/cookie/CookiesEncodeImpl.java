package com.hundsun.jresplus.web.nosession.cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.SerializationException;

import com.hundsun.jresplus.common.util.StringUtil;

public class CookiesEncodeImpl implements Encode {

	public String encode(Object object) throws SerializationException {
		if (object == null) {
			return null;
		}
		return new String(Base64.encodeBase64(HessianZipSerializer
				.encode(object)));
	}

	public Object decode(String str) throws SerializationException {
		if (StringUtil.isEmpty(str) == true) {
			return null;
		}
		return HessianZipSerializer.decode(Base64.decodeBase64(str));
	}

}
