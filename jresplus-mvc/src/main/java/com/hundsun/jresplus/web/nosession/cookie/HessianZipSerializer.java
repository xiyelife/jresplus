/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hundsun.jresplus.web.nosession.cookie;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.lang.SerializationException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.hundsun.jresplus.common.util.io.ByteArrayInputStream;
import com.hundsun.jresplus.common.util.io.ByteArrayOutputStream;

public class HessianZipSerializer {
	public static byte[] encode(Object object) throws SerializationException {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Deflater def = new Deflater(Deflater.BEST_COMPRESSION, false);
		DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);

		Hessian2Output ho = null;

		try {
			ho = new Hessian2Output(dos);
			ho.writeObject(object);
		} catch (Exception e) {
			throw new SerializationException("Failed to encode date", e);
		} finally {
			if (ho != null) {
				try {
					ho.close();
				} catch (IOException e) {
				}
			}
			try {
				dos.close();
			} catch (IOException e) {
			}

			def.end();
		}

		return baos.toByteArray();
	}

	public static Object decode(byte[] encodedValue)
			throws SerializationException {
		ByteArrayInputStream bais = new ByteArrayInputStream(encodedValue);
		Inflater inf = new Inflater(false);
		InflaterInputStream iis = new InflaterInputStream(bais, inf);
		Hessian2Input hi = null;
		try {
			hi = new Hessian2Input(iis);
			return hi.readObject();
		} catch (Exception e) {
			throw new SerializationException("Failed to parse data", e);
		} finally {
			if (hi != null) {
				try {
					hi.close();
				} catch (IOException e) {
				}
			}
			try {
				iis.close();
			} catch (IOException e) {
			}
			inf.end();
		}
	}
}
