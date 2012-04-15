package com.inotai.jasmine;

import com.inotai.jasmine.reader.Flags;
import com.inotai.jasmine.reader.Helpers;
import com.inotai.jasmine.reader.Token;
import com.inotai.jasmine.reader.TokenType;
import com.inotai.jasmine.value.DictionaryValue;
import com.inotai.jasmine.value.ListValue;
import com.inotai.jasmine.value.StringType;
import com.inotai.jasmine.value.Value;

public class JasmineReader {

	static final String STR_NULL = "null";
	static final String STR_TRUE = "true";
	static final String STR_FALSE = "false";

	static final Token TOKEN_INVALID = new Token(TokenType.T_Invalid, 0, 0);

	public static class LinePos {

		public int line;

		public int linePos;

		public int offset;

	}

	private final static int STACK_LIMIT = 100;

	private CharSequence json;

	// Pointer to the starting position in the input string.
	int m_pos_start;

	// Pointer to the current position in the input string.
	int m_pos;

	// Used to keep track of how many nested lists/dicts there are.
	int m_stack_depth;

	// Parser flags
	private final Flags m_flags;

	private JasmineReader(Flags flags) {
		m_flags = flags;
	}

	public static Value read(CharSequence input) {
		return new JasmineReader(Flags.getJasmine()).toValue(input, true);

	}

	private Value buildValue(boolean checkRoot) {
		m_stack_depth++;
		if (m_stack_depth > STACK_LIMIT) {
			LinePos begin = new LinePos();
			// findLinePos( m_pos, begin );
			// throw new JsonTooMuchNestingException(begin, begin);
		}

		Token token = new Token();
		parseToken(token);
		// The root token must be an array or an object.
		// if( is_root && token.type != TokenType.T_ObjectBegin && token.type !=
		// TokenType.T_ArrayBegin )
		{
			LinePos begin, end;
			// findLinePos( token, &begin, &end );
			// throw JsonBadRootElementTypeException( begin, end );
		}

		Value node = null;

		switch (token.getType()) {
		case T_Invalid: {
			// LinePos begin, end;
			// findLinePos( token, &begin, &end );
			// throw JsonUnexpectedTokenException( begin, end );
		}
			return null;

		case T_EndOfInput: {
			// LinePos begin;
			// findLinePos( token.begin, &begin );
			// throw JsonUnexpectedlyTerminatedException( begin, begin );
		}
			return null;

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
				Value array_node = buildValue(false);

				if (array_node == null)
					return null;

				((ListValue) node).add(array_node);

				// After a list value, we expect a comma or the end of the list.
				parseToken(token);

				if (token.getType() == TokenType.T_ListSeparator) {
					Token token_comma = new Token(token);

					m_pos += token.getLength();
					parseToken(token);

					// Trailing commas are invalid according to the JSON RFC,
					// but some
					// consumers need the parsing leniency, so handle
					// accordingly.
					if (token.getType() == TokenType.T_ArrayEnd) {
						if (!m_flags.isComma_trailing()) {
							// LinePos begin, end;
							// findLinePos( token_comma, &begin, &end );
							// throw JsonTrailingCommaException( begin, end );
						}

						// Trailing comma OK, stop parsing the Array.
						break;
					}

				} else if (m_flags.isComma_omit() == false
						&& token.getType() != TokenType.T_ArrayEnd) {
					// Unexpected value after list value. Bail out.
					return null;
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
				if (m_flags.isSymbols()
						&& token.getType() == TokenType.T_Number) {
					token.setType(TokenType.T_Symbol);
				}

				if (!token_type_is_in(token.getType(),
						TokenType.T_StringDoubleQuoted,
						TokenType.T_StringSingleQuoted, TokenType.T_Symbol)) {
					// LinePos begin, end;
					// findLinePos( token, &begin, &end );
					// throw JsonInvalidDictionaryKeyException( begin, end );
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
				Value dict_value = buildValue(false);
				if (dict_value == null)
					return null;

				// Add key and value to the dictionary
				((DictionaryValue) node).add(dict_key, dict_value);

				// Get list separator
				// @todo Make this optional
				parseToken(token);
				if (token.getType() == TokenType.T_ListSeparator) {
					Token token_comma = new Token(token);

					// Check what's next
					m_pos += token.getLength();
					parseToken(token);

					// Trailing commas are invalid according to the JSON RFC,
					// but some
					// consumers need the parsing leniency, so handle
					// accordingly.
					if (token.getType() == TokenType.T_ObjectEnd) {
						if (!m_flags.isComma_trailing()) {
							// LinePos begin, end;
							// findLinePos( token_comma, &begin, &end );
							// throw JsonTrailingCommaException( begin, end );
						}
						// Trailing comma OK, stop parsing the Object.
						break;
					}
				} else if (m_flags.isComma_omit() == false
						&& token.getType() != TokenType.T_ObjectEnd) {
					// Unexpected value after last object value. Bail out.
					return null;
				}
			}

			// An object end token must be in token after the loop
			if (token.getType() != TokenType.T_ObjectEnd)
				return null;

			break;
		}

		default: {
			// LinePos begin, end;
			// findLinePos( token, &begin, &end );
			// throw JsonSyntaxException( begin, end );
		}
		}

		m_pos += token.getLength();

		--m_stack_depth;
		return node;

	}

	public Value toValue(CharSequence aJson, boolean checkRoot) {

		json = aJson;
		Value root = buildValue(checkRoot);
		if (root != null) {
			/*
			 * parseToken( token );
			 * 
			 * if( token.type == TokenType.T_EndOfInput ) return root.release();
			 * else { LinePos begin, end; findLinePos( token, &begin, &end );
			 * throw JsonUnexpectedDataAfterRoot( begin, end ); }
			 */
		}

		return root;

	}

	void parseToken(Token o_token)
	// ------------------------------------------------------------------------
	{
		eatWhitespaceAndComments();

		o_token.set(TokenType.T_Invalid, 0, 0);
		switch (json.charAt(m_pos)) {
		case '\0':
			o_token.setType(TokenType.T_EndOfInput);
			break;

		case 'n':
			if (nextStringMatch(STR_NULL))
				o_token.set(TokenType.T_Null, m_pos, 4);
			else if (m_flags.isSymbols())
				parseSymbol(o_token);
			break;

		case 't':
			if (nextStringMatch(STR_TRUE))
				o_token.set(TokenType.T_BoolTrue, m_pos, 4);
			else if (m_flags.isSymbols())
				parseSymbol(o_token);
			break;

		case 'f':
			if (nextStringMatch(STR_FALSE))
				o_token.set(TokenType.T_BoolFalse, m_pos, 5);
			else if (m_flags.isSymbols())
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
			if (m_flags.isSymbols() && o_token.getType() == TokenType.T_Invalid)
				parseSymbol(o_token);
			break;

		case '"':
			parseStringDoubleQuoted(o_token);
			break;

		case '\'':
			if (m_flags.isString_single_quoted()) {
				parseStringSingleQuoted(o_token);
			}
			break;

		case '/':
			if (m_flags.isReg_exp()) {
				parseRegExp(o_token);
			}
			break;

		default:
			if (m_flags.isSymbols()) {
				parseSymbol(o_token);
			}
		}

		return;
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
			LinePos begin, end;
			// findLinePos( o_token, &begin, &end );
			// throw JsonUnexpectedlyTerminatedException( begin, end );
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
			o_token = TOKEN_INVALID;
			return;
		}

		// Optional fraction part
		c = o_token.nextChar(json);
		if ('.' == c) {
			o_token.incLength();
			if (!Helpers.read_int(o_token, json, true)) {
				o_token = TOKEN_INVALID;
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
				o_token = TOKEN_INVALID;
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
						LinePos begin, end;
						// findLinePos( m_pos + o_token.getLength() - 2, m_pos +
						// o_token.getLength(), begin, end );
						// throw JsonInvalidEscapeInStringException( 'x', begin,
						// end );
					}
					break;

				case 'u':
					if (!Helpers.read_hex_digits(o_token, json, 4)) {
						LinePos begin, end;
						// findLinePos( m_pos + o_token.length - 2, m_pos +
						// o_token.length, &begin, &end );
						// throw JsonInvalidEscapeInStringException( 'u', begin,
						// end );
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
					LinePos begin,
					end;
					// findLinePos( m_pos + o_token.length - 2, m_pos +
					// o_token.length, &begin, &end );
					// throw JsonInvalidEscapeInStringException( c, begin, end
					// );
				}

			} else if ('"' == c) {
				o_token.incLength();
				return;
			}

			o_token.incLength();
			c = o_token.nextChar(json);
		}

		o_token = TOKEN_INVALID;
		return;
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

		o_token = TOKEN_INVALID;
		return;
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

		return Value.createStringValue(decoded_str.toString());
	}

	boolean nextStringMatch(String str) {
		for (int i = 0; i < str.length(); ++i) {
			if ('\0' == json.charAt(m_pos))
				return false;

			if (json.charAt(m_pos + 1) != str.charAt(i))
				return false;
		}

		return true;
	}

	void eatWhitespaceAndComments() {
		while ('\0' != json.charAt(m_pos)) {
			switch (json.charAt(m_pos)) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				++m_pos;
				break;
			case '/':
				// TODO(tc): This isn't in the RFC so it should be a parser
				// flag.
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
			while ('\0' != json.charAt(m_pos)) {
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
			while ('\0' != json.charAt(m_pos)) {
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

}