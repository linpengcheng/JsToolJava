package org.sunjw.js.util;

import java.io.Serializable;

public class TokenBuffer implements Serializable, CharSequence {

	private static final long serialVersionUID = 4063766941723794799L;

	private StringBuffer mValue;

	public TokenBuffer() {
		mValue = new StringBuffer();
	}

	public TokenBuffer(String string) {
		mValue = new StringBuffer(string);
	}

	public TokenBuffer(char ch) {
		mValue = new StringBuffer();
		mValue.append(ch);
	}

	public StringBuffer append(boolean b) {
		return mValue.append(b);
	}

	public StringBuffer append(char c) {
		return mValue.append(c);
	}

	public StringBuffer append(char[] str) {
		return mValue.append(str);
	}

	public StringBuffer append(char[] str, int offset, int len) {
		return mValue.append(str, offset, len);
	}

	public StringBuffer append(CharSequence s) {
		return mValue.append(s);
	}

	public StringBuffer append(CharSequence s, int start, int end) {
		return mValue.append(s, start, end);
	}

	public StringBuffer append(double d) {
		return mValue.append(d);
	}

	public StringBuffer append(float f) {
		return mValue.append(f);
	}

	public StringBuffer append(int i) {
		return mValue.append(i);
	}

	public StringBuffer append(long lng) {
		return mValue.append(lng);
	}

	public StringBuffer append(Object obj) {
		return mValue.append(obj);
	}

	public StringBuffer append(String str) {
		return mValue.append(str);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof String) {
			String anotherString = (String) anObject;
			int n = mValue.length();
			if (n == anotherString.length()) {
				int i = 0;
				int j = 0;
				while (n-- != 0) {
					if (mValue.charAt(i++) != anotherString.charAt(j++))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public char charAt(int index) {
		return mValue.charAt(index);
	}

	@Override
	public int length() {
		return mValue.toString().length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return mValue.subSequence(start, end);
	}

	@Override
	public String toString() {
		return mValue.toString();
	}
}
