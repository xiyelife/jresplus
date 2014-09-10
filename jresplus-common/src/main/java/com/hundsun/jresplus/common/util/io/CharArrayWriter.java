package com.hundsun.jresplus.common.util.io;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public final class CharArrayWriter extends Writer {

	private static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/** 每个buffer的长度,缺省为1K */
	private int bufferEntrySize = 1024;

	private List<char[]> buffers = new ArrayList<char[]>();

	/** 当前使用的buffer下标. */
	private int currentBufferIndex;
	/** 当前buffer */
	private char[] currentBuffer;

	/** 已写入的数据长度 */
	private int count;

	/** 每次写的统计 ,统计10次吧 */
	private Turn turn = new Turn(10);

	public CharArrayWriter() {
		needNewBuffer();
	}

	public CharArrayWriter(int bufferEntrySize) {
		this.bufferEntrySize = bufferEntrySize;
	}

	public int getBufferEntrySize() {
		return this.bufferEntrySize;
	}

	private char[] getBuffer(int index) {
		return buffers.get(index);
	}

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

	private void needNewBuffer() {
		if (currentBufferIndex < buffers.size() - 1) {
			// 还没有未使用的buffer，继续使用老的buffer
			currentBufferIndex++;
			currentBuffer = getBuffer(currentBufferIndex);
		} else {
			// 创建新buffer
			char[] createOne = new char[bufferEntrySize];
			buffers.add(createOne);
			currentBuffer = createOne;
			currentBufferIndex++;
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if ((off < 0) || (off > cbuf.length) || (len < 0)
				|| ((off + len) > cbuf.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		int remaining = len;
		int inBufferPos = getCurrentBufferPos();
		while (remaining > 0) {
			int part = Math.min(remaining, bufferEntrySize - inBufferPos);
			System.arraycopy(cbuf, off + len - remaining, currentBuffer,
					inBufferPos, part);
			remaining -= part;
			count += part;
			if (remaining > 0) {
				inBufferPos = getCurrentBufferPos();
			}
		}
	}

	@Override
	public void write(int b) {
		int inBufferPos = getCurrentBufferPos();
		currentBuffer[inBufferPos] = (char) b;
		count++;
	}

	public int size() {
		return count;
	}

	public void reset() {
		int leftBufferCount = turn.addCountGetAvg();
		count = 0;
		currentBufferIndex = 0;
		currentBuffer = getBuffer(0);
		curtailBuffer(leftBufferCount);
	}

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

	public void writeTo(Writer out) throws IOException {
		int remaining = count;
		for (int i = 0; i < buffers.size(); i++) {
			char[] buf = getBuffer(i);
			int c = Math.min(buf.length, remaining);
			out.write(buf, 0, c);
			remaining -= c;
			if (remaining == 0) {
				break;
			}
		}
	}

	public char[] toCharArray() {
		int remaining = count;
		if (remaining == 0) {
			return EMPTY_CHAR_ARRAY;
		}
		char newbuf[] = new char[remaining];
		int pos = 0;
		for (int i = 0; i < buffers.size(); i++) {
			char[] buf = getBuffer(i);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.count);
		int remaining = this.count;
		for (int i = 0; i < buffers.size(); i++) {
			char[] buf = getBuffer(i);
			int c = Math.min(buf.length, remaining);
			sb.append(buf, 0, c);
			remaining -= c;
			if (remaining == 0) {
				break;
			}
		}
		return sb.toString();
	}

	@Override
	public void flush() throws IOException {
		// nop
	}

	@Override
	public void close() throws IOException {
		// nop
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
