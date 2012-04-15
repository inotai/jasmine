package com.inotai.jasmine;

import com.inotai.jasmine.reader.Helpers;
import com.inotai.jasmine.reader.Token;
import com.inotai.jasmine.reader.TokenType;
import com.inotai.jasmine.reader.error.BadRootElementTypeException;
import com.inotai.jasmine.reader.error.InvalidDictionaryKeyException;
import com.inotai.jasmine.reader.error.InvalidEscapeInStringException;
import com.inotai.jasmine.reader.error.ParserException;
import com.inotai.jasmine.reader.error.TooMuchNestingException;
import com.inotai.jasmine.reader.error.UnexpectedDataAfterRootException;
import com.inotai.jasmine.reader.error.UnexpectedTokenException;
import com.inotai.jasmine.reader.error.UnexpectedlyTerminatedException;
import com.inotai.jasmine.value.DictionaryValue;
import com.inotai.jasmine.value.ListValue;
import com.inotai.jasmine.value.StringType;
import com.inotai.jasmine.value.Value;

public class JasmineReader {

	static final String STR_NULL = "null";
	static final String STR_TRUE = "true";
	static final String STR_FALSE = "false";

	public static class LinePos {

		public int line;

		public int column;

		public void set(LinePos o) {
			line = o.line;
			column = o.column;
		}

	}

	private final static int STACK_LIMIT = 100;

	private CharSequence json;

	// Pointer to the current position in the input string.
	int m_pos;

	// Used to keep track of how many nested lists/dicts there are.
	int m_stack_depth;

	public static Value read(CharSequence input) {
		return new JasmineReader().toValue(input, true);
	}

	private Value buildValue(Token token) {
		m_stack_depth++;
		if (m_stack_depth > STACK_LIMIT) {
			LinePos begin = findLinePos(m_pos);
			throw new TooMuchNestingException(begin.line, begin.column);
		}

		Value node = null;

		switch (token.getType()) {
		case T_Invalid: {
			LinePos linePos = findLinePos(token);
			throw new UnexpectedTokenException(linePos.line, linePos.column);
		}

		case T_EndOfInput: {
			throw new UnexpectedlyTerminatedException();
		}

		case T_Null:
			node = Value.createNullValue();
			break;

		case T_BoolTrue:
			node = Value.createBooleanValue(true);
			break;

		case T_BoolFalse:
			node = Value.createBooleanValue(false);
			break;

		case T_Number:
			node = decodeNumber(token);
			if (node == null)
				return null;
			break;

		case T_StringDoubleQuoted:
			node = decodeStringDoubleQuoted(token);
			if (node == null)
				return null;
			break;

		case T_StringSingleQuoted:
			node = decodeStringSingleQuoted(token);
			if (node == null)
				return null;
			break;

		case T_RegExp:
			node = decodeRegExp(token);
			if (node == null)
				return null;
			break;

		case T_Symbol:
			node = decodeSymbol(token);
			if (node == null)
				return null;
			break;

		case T_ArrayBegin: {
			m_pos += token.getLength();
			parseToken(token);

			node = Value.createListValue();

			while (token.getType() != TokenType.T_ArrayEnd) {
				Value array_node = buildValue(token);

				if (array_node == null)
					return null;

				((ListValue) node).add(array_node);

				// After a list value, we expect a comma or the end of the list.
				parseToken(token);

				if (token.getType() == TokenType.T_ListSeparator) {
					m_pos += token.getLength();
					parseToken(token);
					if (token.getType() == TokenType.T_ArrayEnd) {
						// Trailing comma OK, stop parsing the Array.
						break;
					}
				}
			}

			if (token.getType() != TokenType.T_ArrayEnd) {
				return null;
			}

			break;
		}

		case T_ObjectBegin: {
			m_pos += token.getLength();
			parseToken(token);

			node = Value.createDictionaryValue();
			while (token.getType() != TokenType.T_ObjectEnd) {
				if (token.getType() == TokenType.T_Number) {
					token.setType(TokenType.T_Symbol);
				}

				if (!token_type_is_in(token.getType(),
						TokenType.T_StringDoubleQuoted,
						TokenType.T_StringSingleQuoted, TokenType.T_Symbol)) {
					LinePos pos = findLinePos(token);
					throw new InvalidDictionaryKeyException(pos.line,
							pos.column);
				}

				// Get key value
				Value dict_key_value = decodeDictionaryKey(token);
				if (dict_key_value == null)
					return null;

				// Get key string
				String dict_key = dict_key_value.getAsString();

				// Key-value pair separator
				m_pos += token.getLength();
				parseToken(token);
				if (token.getType() != TokenType.T_ObjectPairSeparator)
					return null;

				// Get key value
				m_pos += token.getLength();
				parseToken(token);
				Value dict_value = buildValue(token);
				if (dict_value == null)
					return null;

				// Add key and value to the dictionary
				((DictionaryValue) node).add(dict_key, dict_value);

				// Get list separator
				parseToken(token);
				if (token.getType() == TokenType.T_ListSeparator) {
					// Check what's next
					m_pos += token.getLength();
					parseToken(token);
					if (token.getType() == TokenType.T_ObjectEnd) {
						// Trailing comma OK, stop parsing the Object.
						break;
					}
				}
			}

			// An object end token must be in token after the loop
			if (token.getType() != TokenType.T_ObjectEnd)
				return null;

			break;
		}

		default: {
			LinePos pos = findLinePos(token);
			throw new ParserException(pos.line, pos.column);
		}
		}

		m_pos += token.getLength();

		--m_stack_depth;
		return node;

	}

	public Value toValue(CharSequence aJson, boolean checkRoot) {

		json = aJson;
		Token token = new Token();
		parseToken(token);
		// The root token must be an array or an object.
		if (checkRoot && token.getType() != TokenType.T_ObjectBegin
				&& token.getType() != TokenType.T_ArrayBegin) {
			throw new BadRootElementTypeException();
		}

		Value root = buildValue(token);
		if (root != null) {
			parseToken(token);

			if (token.getType() == TokenType.T_EndOfInput)
				return root;
			else {
				LinePos pos = findLinePos(token);
				throw new UnexpectedDataAfterRootException(pos.line, pos.column);
			}

		}

		Helpers.not_reached();
		return null;

	}

	void parseToken(Token o_token) {
		eatWhitespaceAndComments();

		o_token.set(TokenType.T_Invalid, 0, 0);

		if (m_pos == json.length()) {
			o_token.setType(TokenType.T_EndOfInput);
			return;
		}

		switch (json.charAt(m_pos)) {
		case 'n':
			if (nextStringMatch(STR_NULL))
				o_token.set(TokenType.T_Null, m_pos, 4);
			else
				parseSymbol(o_token);
			break;

		case 't':
			if (nextStringMatch(STR_TRUE))
				o_token.set(TokenType.T_BoolTrue, m_pos, 4);
			else
				parseSymbol(o_token);
			break;

		case 'f':
			if (nextStringMatch(STR_FALSE))
				o_token.set(TokenType.T_BoolFalse, m_pos, 5);
			else
				parseSymbol(o_token);
			break;

		case '[':
			o_token.set(TokenType.T_ArrayBegin, m_pos, 1);
			break;

		case ']':
			o_token.set(TokenType.T_ArrayEnd, m_pos, 1);
			break;

		case ',':
			o_token.set(TokenType.T_ListSeparator, m_pos, 1);
			break;

		case '{':
			o_token.set(TokenType.T_ObjectBegin, m_pos, 1);
			break;

		case '}':
			o_token.set(TokenType.T_ObjectEnd, m_pos, 1);
			break;

		case ':':
			o_token.set(TokenType.T_ObjectPairSeparator, m_pos, 1);
			break;

		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			parseNumberToken(o_token);
			break;

		case '-':
			parseNumberToken(o_token);
			if (o_token.getType() == TokenType.T_Invalid)
				parseSymbol(o_token);
			break;

		case '"':
			parseStringDoubleQuoted(o_token);
			break;

		case '\'':
			parseStringSingleQuoted(o_token);
			break;

		case '/':
			parseRegExp(o_token);
			break;

		default:
			parseSymbol(o_token);
		}
	}

	void parseRegExp(Token o_token) {
		o_token.set(TokenType.T_RegExp, m_pos, 1);
		char c = o_token.nextChar(json);
		while ('\0' != c) {
			if ('\\' == c) {
				o_token.incLength();
				c = o_token.nextChar(json);
			} else if ('/' == c) {
				o_token.incLength();
				break;
			}

			o_token.incLength();
			c = o_token.nextChar(json);
		}

		if ('/' != c) {
			LinePos pos = findLinePos(o_token);
			throw new UnexpectedlyTerminatedException(pos.line, pos.column);
		}

		c = o_token.nextChar(json);
		while ('\0' != c && is_jasmine_symbol_char(c)) {
			o_token.incLength();
			c = o_token.nextChar(json);
		}
	}

	Value decodeRegExp(Token token) {
		StringBuffer decoded_reg_ex = new StringBuffer();
		;

		int i = 1;
		for (; i < token.getLength() - 1; ++i) {
			char c = json.charAt(token.getBegin() + i);

			if ('\\' == c) {
				// Not escaped
				decoded_reg_ex.append(c);
				// Read the escaped character
				i++;
				c = json.charAt(token.getBegin() + i);
				decoded_reg_ex.append(c);
			} else if ('/' == c) {
				// End of regexp, options may continue
				break;
			} else {
				decoded_reg_ex.append(c);
			}
		}

		StringBuffer decoded_options = new StringBuffer();
		// Skip '/'
		i++;

		for (; i < token.getLength(); ++i) {
			decoded_options.append(json.charAt(token.getBegin() + i));
		}

		return Value.createRegExpValue(decoded_reg_ex.toString(),
				decoded_options.toString());
	}

	Value decodeSymbol(Token token) {
		return Value.createStringValue(token.getString(json),
				StringType.UNQUOTED);
	}

	Value decodeDictionaryKey(Token token) {
		switch (token.getType()) {
		case T_StringDoubleQuoted:
			return decodeStringDoubleQuoted(token);
		case T_StringSingleQuoted:
			return decodeStringSingleQuoted(token);
		case T_Symbol:
			return decodeSymbol(token);
		}

		Helpers.not_reached();
		return null;
	}

	void parseSymbol(Token o_token) {
		char c = json.charAt(m_pos);
		if (is_jasmine_symbol_char(c)) {
			o_token.set(TokenType.T_Symbol, m_pos, 0);

			do {
				o_token.incLength();
				c = o_token.nextChar(json);

			} while (is_jasmine_symbol_char(c));
		}
	}

	void parseNumberToken(Token o_token) {
		// We just grab the number here. We validate the size in DecodeNumber.
		// According to RFC4627, a valid number is: [minus] int [frac] [exp]
		o_token.set(TokenType.T_Number, m_pos, 0);

		char c = json.charAt(m_pos);
		if ('-' == c) {
			o_token.incLength();
			c = o_token.nextChar(json);
		}

		if (!Helpers.read_int(o_token, json, false)) {
			o_token.setType(TokenType.T_Invalid);
			return;
		}

		// Optional fraction part
		c = o_token.nextChar(json);
		if ('.' == c) {
			o_token.incLength();
			if (!Helpers.read_int(o_token, json, true)) {
				o_token.setType(TokenType.T_Invalid);
				return;
			}
			c = o_token.nextChar(json);
		}

		// Optional exponent part
		if ('e' == c || 'E' == c) {
			o_token.incLength();
			c = o_token.nextChar(json);
			if ('-' == c || '+' == c) {
				o_token.incLength();
				c = o_token.nextChar(json);
			}

			if (!Helpers.read_int(o_token, json, true)) {
				o_token.setType(TokenType.T_Invalid);
				return;
			}
		}
	}

	Value decodeNumber(Token token) {
		try {
			int num_int = Helpers.string_to_int(token.getBegin(), json,
					token.getLength());
			return Value.createIntegerValue(num_int);
		} catch (Exception e) {
			// ignore
		}
		try {
			double num_double = Helpers.string_to_double(token.getBegin(),
					json, token.getLength());
			if (Helpers.is_finite(num_double)) {
				return Value.createRealValue(num_double);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	void parseStringDoubleQuoted(Token o_token) {
		o_token.set(TokenType.T_StringDoubleQuoted, m_pos, 1);
		char c = o_token.nextChar(json);
		while ('\0' != c) {
			if ('\\' == c) {
				o_token.incLength();
				c = o_token.nextChar(json);
				// Make sure the escaped char is valid.
				switch (c) {
				case 'x':
					if (!Helpers.read_hex_digits(o_token, json, 2)) {
						LinePos pos = findLinePos(m_pos + o_token.getLength()
								- 2);
						throw new InvalidEscapeInStringException('x', pos.line,
								pos.column);
					}
					break;

				case 'u':
					if (!Helpers.read_hex_digits(o_token, json, 4)) {
						LinePos pos = findLinePos(m_pos + o_token.getLength()
								- 2);
						throw new InvalidEscapeInStringException('u', pos.line,
								pos.column);
					}
					break;

				case '\\':
				case '/':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
				case 'v':
				case '"':
					break;

				default:
					LinePos pos = findLinePos(m_pos + o_token.getLength() - 2);
					throw new InvalidEscapeInStringException(c, pos.line,
							pos.column);
				}

			} else if ('"' == c) {
				o_token.incLength();
				return;
			}

			o_token.incLength();
			c = o_token.nextChar(json);
		}
		o_token.setType(TokenType.T_Invalid);
	}

	Value decodeStringDoubleQuoted(Token token) {
		StringBuilder decoded_str = new StringBuilder();

		for (int i = 1; i < token.getLength() - 1; ++i) {
			char c = json.charAt(token.getBegin() + i);
			if ('\\' == c) {
				++i;
				c = json.charAt(token.getBegin() + i);
				switch (c) {
				case '"':
				case '/':
				case '\\':
					decoded_str.append(c);
					break;
				case 'b':
					decoded_str.append('\b');
					break;
				case 'f':
					decoded_str.append('\f');
					break;
				case 'n':
					decoded_str.append('\n');
					break;
				case 'r':
					decoded_str.append('\r');
					break;
				case 't':
					decoded_str.append('\t');
					break;
				// case 'v':
				// decoded_str.append('\v');
				// break;

				case 'x':
					decoded_str.append((Helpers.hex_to_int(json.charAt(token
							.getBegin() + i + 1)) << 4)
							+ Helpers.hex_to_int(json.charAt(token.getBegin()
									+ i + 2)));
					i += 2;
					break;

				case 'u':
					decoded_str.append((Helpers.hex_to_int(json.charAt(token
							.getBegin() + i + 1)) << 12)
							+ (Helpers.hex_to_int(json.charAt(token.getBegin()
									+ i + 2)) << 8)
							+ (Helpers.hex_to_int(json.charAt(token.getBegin()
									+ i + 3)) << 4)
							+ Helpers.hex_to_int(json.charAt(token.getBegin()
									+ i + 4)));
					i += 4;
					break;

				default:
					// We should only have valid strings at this point. If not,
					// ParseStringToken didn't do it's job.
					Helpers.not_reached();
					return null;
				}

			} else {
				// Not escaped
				decoded_str.append(c);
			}
		}

		return Value.createStringValue(decoded_str.toString());
	}

	void parseStringSingleQuoted(Token o_token) {
		o_token.set(TokenType.T_StringSingleQuoted, m_pos, 1);
		char c = o_token.nextChar(json);

		while ('\0' != c) {
			if ('\'' == c) {
				o_token.incLength();
				c = o_token.nextChar(json);
				if ('\'' != c) {
					return;
				}
			}

			o_token.incLength();
			c = o_token.nextChar(json);
		}
		o_token.setType(TokenType.T_Invalid);
	}

	Value decodeStringSingleQuoted(Token token) {
		StringBuilder decoded_str = new StringBuilder();

		for (int i = 1; i < token.getLength() - 1; ++i) {
			char c = json.charAt(token.getBegin() + i);
			if ('\'' == c) {
				++i;
				c = json.charAt(token.getBegin() + i);
				if (c == '\'') {
					decoded_str.append(c);
				}
			} else {
				// Not escaped
				decoded_str.append(c);
			}
		}

		return Value.createStringValue(decoded_str.toString(),
				StringType.SINGLE_QUOTED);
	}

	boolean nextStringMatch(String str) {
		for (int i = 0; i < str.length(); ++i) {
			if (m_pos == json.length())
				return false;

			if (json.charAt(m_pos + i) != str.charAt(i))
				return false;
		}

		return true;
	}

	void eatWhitespaceAndComments() {
		while (m_pos != json.length()) {
			switch (json.charAt(m_pos)) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				++m_pos;
				break;
			case '/':
				if (!eatComment())
					return;
				break;

			default:
				// Not a whitespace char, just exit.
				return;
			}
		}
	}

	boolean eatComment() {
		if ('/' != json.charAt(m_pos))
			return false;

		char next_char = json.charAt(m_pos + 1);
		if ('/' == next_char) {
			// Line comment, read until \n or \r
			m_pos += 2;
			while (m_pos != json.length()) {
				switch (json.charAt(m_pos)) {
				case '\n':
				case '\r':
					++m_pos;
					return true;
				default:
					++m_pos;
				}
			}
		} else if ('*' == next_char) {
			// Block comment, read until */
			m_pos += 2;
			while (m_pos != json.length()) {
				if ('*' == json.charAt(m_pos) && '/' == json.charAt(m_pos + 1)) {
					m_pos += 2;
					return true;
				}
				++m_pos;
			}
		} else {
			return false;
		}

		return true;
	}

	// / Returns true if given character is a valid Jasmine symbol character.
	boolean is_jasmine_symbol_char(char c) {
		return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9') || char_is_in(c, '_', '-', '.'));
	}

	boolean token_type_is_in(TokenType t, TokenType... options) {
		for (TokenType o : options) {
			if (t == o) {
				return true;
			}
		}
		return false;
	}

	boolean char_is_in(char c, char... options) {
		for (char o : options) {
			if (c == o) {
				return true;
			}
		}
		return false;
	}

	LinePos findLinePos(int offset) {
		LinePos linePos = new LinePos();
		int end = offset;
		int pos = 0;
		// Figure out the line and column the error occurred at.
		for (; pos != end; ++pos) {
			if (pos == json.length()) {
				Helpers.not_reached();
			}

			if (json.charAt(pos) == '\n') {
				linePos.line++;
				linePos.column = 1;
			} else {
				linePos.column++;
			}
		}
		return linePos;
	}

	private LinePos findLinePos(Token token) {
		return findLinePos(token.getBegin());
	}

}