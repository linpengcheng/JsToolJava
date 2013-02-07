package org.sunjw.js;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.sunjw.js.util.CodeBuffer;

/**
 * JsFormatter <br>
 * A JavaScript formatter(beautify) in Java. <br>
 * Based on realjsformatter.h & realjsformatter.cpp.<br>
 * <br>
 * Copyright (c) 2012-
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * @author Sun Junwen
 * @date 2013-2-6
 * @version 0.9
 * 
 */
public abstract class JsFormatter extends JsParser {

	public boolean mDebugOutput;

	private long mStartTime;
	private long mEndTime;
	private long mDuration;

	private int mNLineIndents;
	private StringBuffer mLineBuffer;

	private Set<String> mSpecKeywordSet; // 后面要跟着括号的关键字集合
	private Map<String, Character> mBlockMap;
	private Stack<Character> mBlockStack;
	private int mNIndents; // 缩进数量，不用计算 blockStack，效果不好

	// 使用栈是为了解决在判断条件中出现循环的问题
	private Stack<Boolean> mBrcNeedStack; // if 之类的后面的括号

	private boolean mBNewLine; // 准备换行的标志
	private boolean mBBlockStmt; // block 真正开始了
	private boolean mBAssign;
	private boolean mBEmptyBracket; // 空 {}

	private boolean mBCommentPut; // 刚刚输出了注释

	private String mInitIndent; // 起始缩进
	private char mChIndent; // 作为缩进的字符
	private int mNChPerInd; // 每个缩进缩进字符个数

	private boolean mBSkipCR; // 读取时跳过 \r
	private boolean mBPutCR; // 使用 \r\n 作为换行

	private boolean mBNLBracket; // { 之前是否换行
	private boolean mBIndentInEmpty; // 是否保持空行的缩进, JSLint 不推荐

	public JsFormatter() {
		mChIndent = '\t';
		mNChPerInd = 1;
		mBSkipCR = false;
		mBPutCR = false;
		mBNLBracket = false;
		mBIndentInEmpty = false;

		init();
	}

	public JsFormatter(char chIndent, int nChPerInd) {
		mChIndent = chIndent;
		mNChPerInd = nChPerInd;
		mBSkipCR = false;
		mBPutCR = false;
		mBNLBracket = false;
		mBIndentInEmpty = false;

		init();
	}

	public JsFormatter(boolean skipCR, boolean putCR) {
		mChIndent = '\t';
		mNChPerInd = 1;
		mBSkipCR = skipCR;
		mBPutCR = putCR;
		mBNLBracket = false;
		mBIndentInEmpty = false;

		init();
	}

	public JsFormatter(char chIndent, int nChPerInd, boolean skipCR,
			boolean putCR, boolean NLBracket, boolean indentInEmpty) {
		mChIndent = chIndent;
		mNChPerInd = nChPerInd;
		mBSkipCR = skipCR;
		mBPutCR = putCR;
		mBNLBracket = NLBracket;
		mBIndentInEmpty = indentInEmpty;

		init();
	}

	/**
	 * 对于 StringUtils.strip(str, " \r\n\t") 的包装
	 * 
	 * @param str
	 * @return
	 */
	public static String trimString(String str) {
		return StringUtils.strip(str, " \r\n\t");
	}

	/**
	 * 对于 StringUtils.strip(str, " \t") 的包装
	 * 
	 * @param str
	 * @return
	 */
	public static String trimSpace(String str) {
		return StringUtils.strip(str, " \t");
	}

	/**
	 * 对于 StringUtils.stripEnd(str, " \t") 的包装
	 * 
	 * @param str
	 * @return
	 */
	public static String trimRightSpace(String str) {
		return StringUtils.stripEnd(str, " \t");
	}

	/**
	 * 对于 StringUtils.replace(str, src, des) 的包装
	 * 
	 * @param str
	 * @param src
	 * @param des
	 * @return
	 */
	public static String stringReplace(String str, String src, String des) {
		return StringUtils.replace(str, src, des);
	}

	private void init() {
		mInitIndent = new String("");

		mDebugOutput = false;

		mTokenCount = 0;

		mLineBuffer = new StringBuffer("");

		mNIndents = 0;
		mNLineIndents = 0;
		mBNewLine = false;
		mBBlockStmt = true;
		mBAssign = false;
		mBEmptyBracket = false;
		mBCommentPut = false;

		mBlockStack = new Stack<Character>();
		mBrcNeedStack = new Stack<Boolean>();

		mBlockMap = new TreeMap<String, Character>();
		mBlockMap.put("if", JS_IF);
		mBlockMap.put("else", JS_ELSE);
		mBlockMap.put("for", JS_FOR);
		mBlockMap.put("do", JS_DO);
		mBlockMap.put("while", JS_WHILE);
		mBlockMap.put("switch", JS_SWITCH);
		mBlockMap.put("case", JS_CASE);
		mBlockMap.put("default", JS_CASE);
		mBlockMap.put("try", JS_TRY);
		mBlockMap.put("finally", JS_TRY); // 等同于 try
		mBlockMap.put("catch", JS_CATCH);
		mBlockMap.put("=", JS_ASSIGN);
		mBlockMap.put("function", JS_FUNCTION);
		mBlockMap.put("{", JS_BLOCK);
		mBlockMap.put("(", JS_BRACKET);
		mBlockMap.put("[", JS_SQUARE);

		mSpecKeywordSet = new TreeSet<String>();
		mSpecKeywordSet.add("if");
		mSpecKeywordSet.add("for");
		mSpecKeywordSet.add("while");
		mSpecKeywordSet.add("switch");
		mSpecKeywordSet.add("catch");
		mSpecKeywordSet.add("function");
		mSpecKeywordSet.add("with");
		mSpecKeywordSet.add("return");
	}

	/**
	 * 派生类需要实现如何输出字符
	 * 
	 * @param ch
	 */
	protected abstract void putChar(char ch);

	private void putToken(String token, String leftStyle, String rightStyle) {
		putString(leftStyle);
		putString(token);
		putString(rightStyle);
		if (!(mBCommentPut && mBNewLine))
			mBCommentPut = false; // 这个一定会发生在注释之后的任何输出后面
	}

	private void putString(String str) {
		int length = str.length();
		for (int i = 0; i < length; ++i) {
			if (mBNewLine
					&& (mBCommentPut || ((mBNLBracket || str.charAt(i) != '{')
							&& str.charAt(i) != ',' && str.charAt(i) != ';'))) {
				// 换行后面不是紧跟着 {,; 才真正换
				putLineBuffer(); // 输出行缓冲

				mLineBuffer = new StringBuffer("");
				mBNewLine = false;
				mNIndents = mNIndents < 0 ? 0 : mNIndents; // 出错修正
				mNLineIndents = mNIndents;
				if (str.charAt(i) == '{' || str.charAt(i) == ','
						|| str.charAt(i) == ';') // 行结尾是注释，使得{,;不得不换行
					--mNLineIndents;
			}

			if (mBNewLine
					&& !mBCommentPut
					&& ((!mBNLBracket && str.charAt(i) == '{')
							|| str.charAt(i) == ',' || str.charAt(i) == ';'))
				mBNewLine = false;

			if (str.charAt(i) == '\n')
				mBNewLine = true;
			else
				mLineBuffer.append(str.charAt(i));
		}
	}

	private void putLineBuffer() {
		CodeBuffer line = new CodeBuffer("");
		line.append(trimRightSpace(mLineBuffer.toString()));

		if (!line.equals("") || mBIndentInEmpty) // Fix "JSLint unexpect space"
													// bug
		{
			for (int i = 0; i < mInitIndent.length(); ++i)
				putChar(mInitIndent.charAt(i)); // 先输出预缩进

			for (int c = 0; c < mNLineIndents; ++c)
				for (int c2 = 0; c2 < mNChPerInd; ++c2)
					putChar(mChIndent); // 输出缩进
		}

		// 加上换行
		if (mBPutCR)
			line.append("\r"); // PutChar('\r');
		line.append("\n"); // PutChar('\n');

		// 输出 line
		for (int i = 0; i < line.length(); ++i)
			putChar(line.charAt(i));
	}

	private void popMultiBlock(char previousStackTop) {
		if (mTokenB.code.equals(";")) // 如果 m_tokenB 是 ;，弹出多个块的任务留给它
			return;

		if (!((previousStackTop == JS_IF && mTokenB.code.equals("else"))
				|| (previousStackTop == JS_DO && mTokenB.code.equals("while")) || (previousStackTop == JS_TRY && mTokenB.code
				.equals("catch")))) {
			Character topStack = null;// = m_blockStack.top();
			if ((topStack = getStackTop(mBlockStack, null)) == null)
				return;
			// ; 还可能可能结束多个 if, do, while, for, try, catch
			while (topStack == JS_IF || topStack == JS_FOR
					|| topStack == JS_WHILE || topStack == JS_DO
					|| topStack == JS_ELSE || topStack == JS_TRY
					|| topStack == JS_CATCH) {
				if (topStack == JS_IF || topStack == JS_FOR
						|| topStack == JS_WHILE || topStack == JS_CATCH
						|| topStack == JS_ELSE || topStack == JS_TRY) {
					mBlockStack.pop();
					--mNIndents;
				} else if (topStack == JS_DO) {
					--mNIndents;
				}

				if ((topStack == JS_IF && mTokenB.code.equals("else"))
						|| (topStack == JS_DO && mTokenB.code.equals("while"))
						|| (topStack == JS_TRY && mTokenB.code.equals("catch")))
					break; // 直到刚刚结束一个 if...else, do...while, try...catch
				// topStack = m_blockStack.top();
				if ((topStack = getStackTop(mBlockStack, null)) == null)
					break;
			}
		}
	}
}
