package com.inotai.jasmine.reader;

public class Token {

	private TokenType type;

	/**
	 * A pointer into JsonReader::json_pos_ that's the beginning of this token.
	 */
	private int begin;

	/**
	 * End should be one char past the end of the token.
	 */
	private int length;

	private final CharSequence input;

	public Token(CharSequence aInput) {
		input = aInput;
		type = TokenType.INVALID;
		begin = 0;
		length = 0;
	}

	public Token(Token orig) {
		input = orig.input;
		type = orig.type;
		begin = orig.begin;
		length = orig.length;
	}

	/**
	 * Get the character that's one past the end of this token. Returns \0 in
	 * case we are at the end of input.
	 */
	public char nextChar() {
		if (begin + length == input.length()) {
			return '\0';
		}
		return input.charAt(begin + length);
	}

	public void set(TokenType aType, int aBegin, int aLength) {
		type = aType;
		begin = aBegin;
		length = aLength;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public TokenType getType() {
		return type;
	}

	public int getBegin() {
		return begin;
	}

	public void incLength() {
		length++;
	}

	public void incLength(int howMany) {
		length += howMany;
	}

	public int getLength() {
		return length;
	}

	public CharSequence getString() {
		return input.subSequence(begin, begin + length);
	}

}
