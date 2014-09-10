package com.hundsun.jresplus.web.velocity.resolver;

/**
 * 
 * @author LeoHu copy by sagahl copy by fish
 * 
 */
public final class LayoutFinder {

	private static final int Seq = '/';

	private char[] layoutName;

	private char[] sameNameLayoutUrl;

	private int lastSeq;

	public LayoutFinder(String screenUrl, char[] layoutPrefix,
			char[] screenPrefix, char[] defaultLayoutName) {
		this.layoutName = defaultLayoutName;
		int layoutPrefixLen = layoutPrefix.length;
		int len = layoutPrefixLen + screenUrl.length() - screenPrefix.length;
		this.sameNameLayoutUrl = new char[len];
		System.arraycopy(layoutPrefix, 0, this.sameNameLayoutUrl, 0,
				layoutPrefixLen);
		int pos = layoutPrefixLen;
		for (int i = screenPrefix.length; i < screenUrl.length(); i++, pos++) {
			this.sameNameLayoutUrl[pos] = screenUrl.charAt(i);
		}
		this.lastSeq = lastIndexOf(len);
	}

	private final int lastIndexOf(int last) {
		for (int i = last - 1; i >= 0; i--) {
			if (sameNameLayoutUrl[i] == Seq) {
				return i;
			}
		}
		return -1;
	}

	public String getSameNameLayoutUrl() {
		return new String(sameNameLayoutUrl);
	}

	public String getLayoutUrl() {
		if (lastSeq == -1) {
			return null;
		}
		int newLen = lastSeq + 1 + this.layoutName.length;
		char[] newChar = new char[newLen];
		System.arraycopy(this.sameNameLayoutUrl, 0, newChar, 0, lastSeq + 1);
		System.arraycopy(this.layoutName, 0, newChar, lastSeq + 1,
				this.layoutName.length);
		lastSeq = lastIndexOf(lastSeq);
		return new String(newChar);
	}
}
