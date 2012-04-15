package com.inotai.jasmine.reader;

import com.inotai.jasmine.reader.error.ParserException;

public class Helpers {

	public static final char[] JASMINE_SYMBOL_SEPARATORS = new char[] { '_',
			'-', '.' };

	public static int hexToInt(char c) {
		if ('0' <= c && c <= '9')
			return c - '0';
		else if ('A' <= c && c <= 'F')
			return c - 'A' + 10;
		else if ('a' <= c && c <= 'f')
			return c - 'a' + 10;
		throw new ParserException(
				"Found non-hex characted where I did not expect it.");
	}

	/**
	 * A helper method for ParseNumberToken. It reads an int from the end of
	 * token. The method returns false if there is no valid integer at the end
	 * of the token.
	 */
	public static boolean readInteger(Token token, CharSequence input,
			boolean canHaveLeadingZeros) {
		char first = token.nextChar(input);
		int len = 0;

		// Read in more digits
		char c = first;
		while ('\0' != c && '0' <= c && c <= '9') {
			token.incLength();
			len++;
			c = token.nextChar(input);
		}

		// We need at least 1 digit.
		if (len == 0)
			return false;

		if (!canHaveLeadingZeros && len > 1 && '0' == first)
			return false;

		return true;
	}

	/**
	 * A helper method for ParseStringToken. It reads |digits| hex digits from
	 * the token. If the sequence if digits is not valid (contains other
	 * characters), the method returns false.
	 */
	public static boolean readHexDigits(Token token, CharSequence input,
			int count) {
		for (int i = 1; i <= count; ++i) {
			int pos = token.getBegin() + token.getLength() + i;
			if (pos == input.length())
				return false;
			char c = input.charAt(pos);
			if (!(('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F')))
				return false;
		}
		token.incLength(count);
		return true;
	}

	public static int stringToInteger(int startPos, CharSequence input,
			int length) {
		int skip = 0;
		while (Character.isWhitespace(input.charAt(startPos + skip))) {
			skip++;
		}
		return Integer.parseInt(input.subSequence(startPos + skip,
				startPos + length).toString());
	}

	public static double stringToDouble(int startPos, CharSequence input,
			int length) {
		int skip = 0;
		while (Character.isWhitespace(input.charAt(startPos + skip))) {
			skip++;
		}
		return Double.parseDouble(input.subSequence(startPos + skip,
				startPos + length).toString());
	}

	/**
	 * Returns true if given character is a valid Jasmine symbol character.
	 */
	public static boolean isJasmineSymbolChar(char c) {
		return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9') || isOneOfCharaceters(c,
				JASMINE_SYMBOL_SEPARATORS));
	}

	public static boolean isOneOfTokenTypes(TokenType t, TokenType[] options) {
		for (TokenType o : options) {
			if (t == o) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOneOfCharaceters(char c, char[] options) {
		for (char o : options) {
			if (c == o) {
				return true;
			}
		}
		return false;
	}

}
