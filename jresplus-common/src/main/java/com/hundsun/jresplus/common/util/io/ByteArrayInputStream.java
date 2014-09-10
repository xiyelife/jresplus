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
package com.hundsun.jresplus.common.util.io;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {
	// buffer from which to read
	private byte[] buffer;
	private int index;
	private int limit;
	private int mark;

	// is the stream closed?
	private boolean closed;

	public ByteArrayInputStream(byte[] data) {
		this(data, 0, data.length);
	}

	public ByteArrayInputStream(byte[] data, int offset, int length) {
		if (data == null) {
			throw new NullPointerException();
		} else if (offset < 0 || offset + length > data.length || length < 0) {
			throw new IndexOutOfBoundsException();
		} else {
			buffer = data;
			index = offset;
			limit = offset + length;
			mark = offset;
		}
	}

	@Override
	public int read() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		} else if (index >= limit) {
			return -1; // EOF
		} else {
			return buffer[index++] & 0xff;
		}
	}

	@Override
	public int read(byte[] data, int offset, int length) throws IOException {
		if (data == null) {
			throw new NullPointerException();
		} else if (offset < 0 || offset + length > data.length || length < 0) {
			throw new IndexOutOfBoundsException();
		} else if (closed) {
			throw new IOException("Stream closed");
		} else if (index >= limit) {
			return -1; // EOF
		} else {
			// restrict length to available data
			if (length > limit - index) {
				length = limit - index;
			}

			// copy out the subarray
			System.arraycopy(buffer, index, data, offset, length);
			index += length;
			return length;
		}
	}

	@Override
	public long skip(long amount) throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		} else if (amount <= 0) {
			return 0;
		} else {
			// restrict amount to available data
			if (amount > limit - index) {
				amount = limit - index;
			}

			index += (int) amount;
			return amount;
		}
	}

	@Override
	public int available() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		} else {
			return limit - index;
		}
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public void mark(int readLimit) {
		mark = index;
	}

	@Override
	public void reset() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		} else {
			// reset index
			index = mark;
		}
	}

	@Override
	public boolean markSupported() {
		return true;
	}
}
