package com.hundsun.jresplus.common.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 线程不安全,智能收缩的动态byteArray实现outputStrem，使用结束后不是用 close方法关闭流，而应该是reset重置
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public final class ByteArrayOutputStream extends OutputStream {

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/** 每个buffer的长度,缺省为5K */
	private int bufferEntrySize = 1024 * 5;

	private List<byte[]> buffers = new ArrayList<byte[]>();
	/** 当前使用的buffer下标. */
	private int currentBufferIndex;
	/** 当前buffer */
	private byte[] currentBuffer;
	/** 已写入的数据长度 */
	private int count;

	/** 每次写的统计 ,统计10次吧 */
	private Turn turn = new Turn(10);

	public ByteArrayOutputStream() {
		needNewBuffer();
	}

	public ByteArrayOutputStream(int bufferEntrySize) {
		this.bufferEntrySize = bufferEntrySize;
	}

	public int getBufferEntrySize() {
		return this.bufferEntrySize;
	}

	/**
	 * Return the appropriate <code>byte[]</code> buffer specified by index.
	 * 
	 * @param index
	 *            the index of the buffer required
	 * @return the buffer
	 */
	private byte[] getBuffer(int index) {
		return buffers.get(index);
	}

	/**
	 * 得到当前buffer中第一个可写的下标
	 * 
	 * @return
	 */
	private int getCurrentBufferPos() {
		if (count == 0) {
			return 0;
		}
		int pos = count % bufferEntrySize;
		if (pos == 0) {
			// 一个buffer用光了
			needNewBuffer();
			return 0;
		}
		return pos;
	}

	/**
	 * 需要一个新的buffer entry
	 */
	private void needNewBuffer() {
		if (currentBufferIndex < buffers.size() - 1) {
			// 还没有未使用的buffer，继续使用老的buffer
			currentBufferIndex++;
			currentBuffer = getBuffer(currentBufferIndex);
		} else {
			// 创建新buffer
			byte[] createOne = new byte[bufferEntrySize];
			buffers.add(createOne);
			currentBuffer = createOne;
			currentBufferIndex++;
		}
	}

	/**
	 * Write the bytes to byte array.
	 * 
	 * @param b
	 *            the bytes to write
	 * @param off
	 *            The start offset
	 * @param len
	 *            The number of bytes to write
	 */
	public void write(byte[] b, int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		int remaining = len;
		int inBufferPos = getCurrentBufferPos();
		while (remaining > 0) {
			int part = Math.min(remaining, bufferEntrySize - inBufferPos);
			System.arraycopy(b, off + len - remaining, currentBuffer,
					inBufferPos, part);
			remaining -= part;
			count += part;
			if (remaining > 0) {
				inBufferPos = getCurrentBufferPos();
			}
		}
	}

	/**
	 * Write a byte to byte array.
	 * 
	 * @param b
	 *            the byte to write
	 */
	public void write(int b) {
		int inBufferPos = getCurrentBufferPos();
		currentBuffer[inBufferPos] = (byte) b;
		count++;
	}

	/**
	 * Writes the entire contents of the specified input stream to this byte
	 * stream. Bytes from the input stream are read directly into the internal
	 * buffers of this streams.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @return total number of bytes read from the input stream (and written to
	 *         this stream)
	 * @throws IOException
	 *             if an I/O error occurs while reading the input stream
	 * @since Commons IO 1.4
	 */
	public int read(InputStream in) throws IOException {
		int readCount = 0;
		int inBufferPos = getCurrentBufferPos();
		int n = in.read(currentBuffer, inBufferPos, bufferEntrySize
				- inBufferPos);
		while (n != -1) {
			readCount += n;
			inBufferPos += n;
			count += n;
			if (inBufferPos == bufferEntrySize) {
				needNewBuffer();
				inBufferPos = 0;
			}
			n = in.read(currentBuffer, inBufferPos, bufferEntrySize
					- inBufferPos);
		}
		return readCount;
	}

	/**
	 * Return the current size of the byte array.
	 * 
	 * @return the current size of the byte array
	 */
	public int size() {
		return count;
	}

	/**
	 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
	 * this class can be called after the stream has been closed without
	 * generating an <tt>IOException</tt>.
	 * 
	 * @throws IOException
	 *             never (this method should not declare this exception but it
	 *             has to now due to backwards compatability)
	 */
	public void close() throws IOException {
		// nop
	}

	/**
	 * @see java.io.ByteArrayOutputStream#reset()
	 */
	public void reset() {
		int leftBufferCount = turn.addCountGetAvg();
		count = 0;
		currentBufferIndex = 0;
		currentBuffer = getBuffer(0);
		curtailBuffer(leftBufferCount);
	}

	/**
	 * clear和reset最大的不同是当前大小不参与容量计算
	 */
	public void clear() {
		count = 0;
		currentBufferIndex = 0;
		currentBuffer = getBuffer(0);
	}

	/**
	 * 缩减buffers.size到n
	 * 
	 * @param n
	 */
	private void curtailBuffer(int n) {
		if (n < 1) {
			return;
		}
		if (this.buffers.size() <= n) {
			return;
		}
		for (int i = this.buffers.size() - 1; i >= n; i--) {
			this.buffers.remove(i);
		}
	}

	/**
	 * Writes the entire contents of this byte stream to the specified output
	 * stream.
	 * 
	 * @param out
	 *            the output stream to write to
	 * @throws IOException
	 *             if an I/O error occurs, such as if the stream is closed
	 * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
	 */
	public void writeTo(OutputStream out) throws IOException {
		int remaining = count;
		for (int i = 0; i < buffers.size(); i++) {
			byte[] buf = getBuffer(i);
			int c = Math.min(buf.length, remaining);
			out.write(buf, 0, c);
			remaining -= c;
			if (remaining == 0) {
				break;
			}
		}
	}

	/**
	 * Gets the curent contents of this byte stream as a byte array. The result
	 * is independent of this stream.
	 * 
	 * @return the current contents of this output stream, as a byte array
	 * @see java.io.ByteArrayOutputStream#toByteArray()
	 */
	public byte[] toByteArray() {
		int remaining = count;
		if (remaining == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		byte newbuf[] = new byte[remaining];
		int pos = 0;
		for (int i = 0; i < buffers.size(); i++) {
			byte[] buf = getBuffer(i);
			int c = Math.min(buf.length, remaining);
			System.arraycopy(buf, 0, newbuf, pos, c);
			pos += c;
			remaining -= c;
			if (remaining == 0) {
				break;
			}
		}
		return newbuf;
	}

	/**
	 * Gets the curent contents of this byte stream as a string.
	 * 
	 * @return the contents of the byte array as a String
	 * @see java.io.ByteArrayOutputStream#toString()
	 */
	public String toString() {
		return new String(toByteArray());
	}

	/**
	 * Gets the curent contents of this byte stream as a string using the
	 * specified encoding.
	 * 
	 * @param enc
	 *            the name of the character encoding
	 * @return the string converted from the byte array
	 * @throws UnsupportedEncodingException
	 *             if the encoding is not supported
	 * @see java.io.ByteArrayOutputStream#toString(String)
	 */
	public String toString(String enc) throws UnsupportedEncodingException {
		return new String(toByteArray(), enc);
	}

	/**
	 * 把当前的byte内容转换为InputStream
	 * 
	 * @return 线程不安全的Input流
	 */
	public InputStream getInputStream() {
		return new InputStream() {
			private int currentPos = 0;
			private int mark = 0;

			@Override
			public int read() throws IOException {
				if (currentPos == count) {
					return -1;
				}
				int currentBufferIndex = currentPos / bufferEntrySize;
				int currenPosInBuffer = currentPos % bufferEntrySize;
				byte cur = buffers.get(currentBufferIndex)[currenPosInBuffer];
				currentPos++;
				return cur & 0xff;
			}

			@Override
			public int available() throws IOException {
				return count - currentPos;
			}

			@Override
			public synchronized void mark(int readlimit) {
				mark = currentPos;
			}

			@Override
			public synchronized void reset() throws IOException {
				currentPos = mark;
			}

			@Override
			public boolean markSupported() {
				return true;
			}

			@Override
			public long skip(long n) throws IOException {
				if (currentPos + n > count) {
					n = count - currentPos;
				}
				if (n < 0) {
					return 0;
				}
				currentPos += n;
				return n;
			}
		};
	}

	public interface BytesVisitor {
		public void visit(byte[] buffer, int length);
	}

	public void visitBytes(BytesVisitor visitor) {
		int lastIndex = count / bufferEntrySize;
		for (int i = 0; i < lastIndex; i++) {
			byte[] buffer = this.buffers.get(i);
			visitor.visit(buffer, buffer.length);
		}
		int left = count % bufferEntrySize;
		if (left > 0) {
			byte[] buffer = this.buffers.get(lastIndex);
			visitor.visit(buffer, left);
		}
	}

	/**
	 * 循环队列
	 * 
	 */
	private final class Turn {
		private Fragment currentFg;

		/**
		 * 创建一个长度为size的循环队列
		 */
		private Turn(int size) {
			Fragment head = new Fragment();
			currentFg = head;
			for (int i = 0; i < size - 1; i++) {
				currentFg.next = new Fragment();
				currentFg = currentFg.next;
			}
			currentFg.next = head;
		}

		/**
		 * 增加一个数据并且统计平均值
		 */
		private int addCountGetAvg() {
			if (count != 0) {
				currentFg.writeCount = count;
				currentFg = currentFg.next;
			}
			long total = 0L;
			int usedFragment = 0;
			Fragment f = currentFg.next;
			while (f != currentFg) {
				total += f.writeCount;
				if (f.writeCount != 0) {
					usedFragment++;
				}
				f = f.next;
			}
			if (usedFragment == 0 || total == 0) {
				return count;
			}
			return (int) (total / usedFragment / bufferEntrySize) + 1;
		}

	}

	private static final class Fragment {
		private int writeCount = 0;
		private Fragment next;
	}

}
