package com.hundsun.jresplus.web.nosession;

import java.security.SecureRandom;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public final class RandomShortUUID implements UUIDGenerator {
	/*
	 * The random number generator used by this class to create random based
	 */
	private static volatile SecureRandom numberGenerator = new SecureRandom();

	private static final int length = 25;

	private static StringBuilder getBuilder() {
		SecureRandom ng = numberGenerator;
		byte[] data = new byte[16];
		ng.nextBytes(data);

		StringBuilder sb = new StringBuilder(length);
		long bits = data[0] & 0xff;
		bits = (bits << 8) | (data[1] & 0xff);
		bits = (bits << 8) | (data[2] & 0xff);
		bits = (bits << 8) | (data[3] & 0xff);
		bits = (bits << 8) | (data[4] & 0xff);
		bits = (bits << 8) | (data[5] & 0xff);
		bits = (bits << 8) | (data[6] & 0xff);
		bits = (bits << 6) | (data[7] & 0x3f);
		append(bits, sb);
		bits = data[8] & 0xff;
		bits = (bits << 8) | (data[9] & 0xff);
		bits = (bits << 8) | (data[10] & 0xff);
		bits = (bits << 8) | (data[11] & 0xff);
		bits = (bits << 8) | (data[12] & 0xff);
		bits = (bits << 8) | (data[13] & 0xff);
		bits = (bits << 8) | (data[14] & 0xff);
		bits = (bits << 4) | (data[15] & 0xf);
		append(bits, sb);
		return sb;
	}

	/**
	 * 
	 */
	public static String getFixSize() {
		StringBuilder sb = getBuilder();
		int needAdd = length - sb.length();
		for (int i = 0; i < needAdd; i++) {
			sb.append('0');
		}
		return sb.toString();
	}

	/**
	 * 
	 */
	public static String get() {
		return getBuilder().toString();
	}

	public String gain() {
		return get();
	}

	final static char[] Digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm',
			'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x' };

	private static final int Radix = Digits.length;

	private static void append(long i, StringBuilder sb) {
		char[] buf = new char[13];
		int charPos = 12;
		i = -i;
		while (i <= -Radix) {
			buf[charPos--] = Digits[(int) (-(i % Radix))];
			i = i / Radix;
		}
		buf[charPos] = Digits[(int) (-i)];
		sb.append(buf, charPos, (13 - charPos));
	}
}
