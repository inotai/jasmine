package com.inotai.jasmine.reader;

public class Token {

	private TokenType type;

	// A pointer into JsonReader::json_pos_ that's the beginning of this token.
	private int begin;

	// End should be one char past the end of the token.
	private int length;

	public Token() {
		type = TokenType.T_Invalid;
		begin = 0;
		length = 0;
	}

	public Token(TokenType t, int b, int len) {
		type = t;
		begin = b;
		length = len;
	}

	public Token(Token orig) {
		type = orig.type;
		begin = orig.begin;
		length = orig.length;
	}

	// Get the character that's one past the end of this token.
	public char nextChar(CharSequence input) {
		if (begin + length == input.length()) {
			return '\0';
		}
		return input.charAt(begin + length);
	}

	// Sets the token
	public void set(TokenType __type, int __begin, int __length) {
		type = __type;
		begin = __begin;
		length = __length;
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

	public CharSequence getString(CharSequence input) {
		return input.subSequence(begin, begin + length);
	}

}
