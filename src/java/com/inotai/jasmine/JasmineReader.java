package com.inotai.jasmine;

import com.inotai.jasmine.reader.Helpers;
import com.inotai.jasmine.reader.LinePos;
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

	public static final int STACK_LIMIT = 100;

	private CharSequence jasmine;

	/**
	 * Pointer to the current position in the input string.
	 */
	int position;

	/**
	 * Used to keep track of how many nested lists/dicts there are.
	 */
	int stackDepth;

	public static Value read(CharSequence input) {
		return new JasmineReader().toValue(input, true);
	}

	private void checkStackDepth() {
		if (stackDepth > STACK_LIMIT) {
			throw new TooMuchNestingException(findLinePos(position));
		}
	}

	private Value buildValue(Token token) {
		stackDepth++;
		checkStackDepth();
		Value value = null;

		switch (token.getType()) {
		case INVALID: {
			throw new UnexpectedTokenException(findLinePos(token));
		}

		case END_OF_INPUT: {
			throw new UnexpectedlyTerminatedException();
		}

		case NULL:
			value = Value.createNullValue();
			break;

		case BOOL_TRUE:
			value = Value.createBooleanValue(true);
			break;

		case BOOL_FALSE:
			value = Value.createBooleanValue(false);
			break;

		case NUMBER:
			value = decodeNumber(token);
			if (value == null)
				return null;
			break;

		case STRING_DOUBLE_QUOTED:
			value = decodeStringDoubleQuoted(token);
			if (value == null)
				return null;
			break;

		case STRING_SINGLE_QUOTED:
			value = decodeStringSingleQuoted(token);
			if (value == null)
				return null;
			break;

		case REG_EXP:
			value = decodeRegExp(token);
			if (value == null)
				return null;
			break;

		case SYMBOL:
			value = decodeSymbol(token);
			if (value == null)
				return null;
			break;

		case ARRAY_BEGIN: {
			position += token.getLength();
			parseToken(token);

			value = Value.createListValue();

			while (token.getType() != TokenType.ARRAY_END) {
				Value array_node = buildValue(token);

				if (array_node == null)
					return null;

				((ListValue) value).add(array_node);

				// After a list value, we expect a comma or the end of the list.
				parseToken(token);

				if (token.getType() == TokenType.LIST_SEPARATOR) {
					position += token.getLength();
					parseToken(token);
					if (token.getType() == TokenType.ARRAY_END) {
						// Trailing comma OK, stop parsing the Array.
						break;
					}
				}
			}

			if (token.getType() != TokenType.ARRAY_END) {
				return null;
			}

			break;
		}

		case OBJECT_BEGIN: {
			position += token.getLength();
			parseToken(token);

			value = Value.createDictionaryValue();
			while (token.getType() != TokenType.OBJECT_END) {
				if (token.getType() == TokenType.NUMBER) {
					token.setType(TokenType.SYMBOL);
				}

				if (!token_type_is_in(token.getType(),
						TokenType.STRING_DOUBLE_QUOTED,
						TokenType.STRING_SINGLE_QUOTED, TokenType.SYMBOL)) {
					throw new InvalidDictionaryKeyException(findLinePos(token));
				}

				// Get key value
				Value dict_key_value = decodeDictionaryKey(token);
				if (dict_key_value == null)
					return null;

				// Get key string
				String dict_key = dict_key_value.getAsString();

				// Key-value pair separator
				position += token.getLength();
				parseToken(token);
				if (token.getType() != TokenType.OBJECT_PAIR_SEPARATOR)
					return null;

				// Get key value
				position += token.getLength();
				parseToken(token);
				Value dict_value = buildValue(token);
				if (dict_value == null)
					return null;

				// Add key and value to the dictionary
				((DictionaryValue) value).add(dict_key, dict_value);

				// Get list separator
				parseToken(token);
				if (token.getType() == TokenType.LIST_SEPARATOR) {
					// Check what's next
					position += token.getLength();
					parseToken(token);
					if (token.getType() == TokenType.OBJECT_END) {
						// Trailing comma OK, stop parsing the Object.
						break;
					}
				}
			}

			// An object end token must be in token after the loop
			if (token.getType() != TokenType.OBJECT_END)
				return null;

			break;
		}

		default: {
			throw new ParserException(findLinePos(token));
		}
		}

		position += token.getLength();

		--stackDepth;
		return value;

	}

	public Value toValue(CharSequence aJson, boolean checkRoot) {

		jasmine = aJson;
		Token token = new Token();
		parseToken(token);
		// The root token must be an array or an object.
		if (checkRoot && token.getType() != TokenType.OBJECT_BEGIN
				&& token.getType() != TokenType.ARRAY_BEGIN) {
			throw new BadRootElementTypeException();
		}

		Value root = buildValue(token);
		if (root != null) {
			parseToken(token);

			if (token.getType() == TokenType.END_OF_INPUT)
				return root;
			else {
				throw new UnexpectedDataAfterRootException(findLinePos(token));
			}

		}

		Helpers.not_reached();
		return null;

	}

	private void parseToken(Token o_token) {
		eatWhitespaceAndComments();

		o_token.set(TokenType.INVALID, 0, 0);

		if (position == jasmine.length()) {
			o_token.setType(TokenType.END_OF_INPUT);
			return;
		}

		switch (jasmine.charAt(position)) {
		case 'n':
			if (nextStringMatch(STR_NULL))
				o_token.set(TokenType.NULL, position, 4);
			else
				parseSymbol(o_token);
			break;

		case 't':
			if (nextStringMatch(STR_TRUE))
				o_token.set(TokenType.BOOL_TRUE, position, 4);
			else
				parseSymbol(o_token);
			break;

		case 'f':
			if (nextStringMatch(STR_FALSE))
				o_token.set(TokenType.BOOL_FALSE, position, 5);
			else
				parseSymbol(o_token);
			break;

		case '[':
			o_token.set(TokenType.ARRAY_BEGIN, position, 1);
			break;

		case ']':
			o_token.set(TokenType.ARRAY_END, position, 1);
			break;

		case ',':
			o_token.set(TokenType.LIST_SEPARATOR, position, 1);
			break;

		case '{':
			o_token.set(TokenType.OBJECT_BEGIN, position, 1);
			break;

		case '}':
			o_token.set(TokenType.OBJECT_END, position, 1);
			break;

		case ':':
			o_token.set(TokenType.OBJECT_PAIR_SEPARATOR, position, 1);
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
			if (o_token.getType() == TokenType.INVALID)
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

	private void parseRegExp(Token o_token) {
		o_token.set(TokenType.REG_EXP, position, 1);
		char c = o_token.nextChar(jasmine);
		while ('\0' != c) {
			if ('\\' == c) {
				o_token.incLength();
				c = o_token.nextChar(jasmine);
			} else if ('/' == c) {
				o_token.incLength();
				break;
			}

			o_token.incLength();
			c = o_token.nextChar(jasmine);
		}

		if ('/' != c) {
			throw new UnexpectedlyTerminatedException(findLinePos(o_token));
		}

		c = o_token.nextChar(jasmine);
		while ('\0' != c && is_jasmine_symbol_char(c)) {
			o_token.incLength();
			c = o_token.nextChar(jasmine);
		}
	}

	private Value decodeRegExp(Token token) {
		StringBuffer decoded_reg_ex = new StringBuffer();
		;

		int i = 1;
		for (; i < token.getLength() - 1; ++i) {
			char c = jasmine.charAt(token.getBegin() + i);

			if ('\\' == c) {
				// Not escaped
				decoded_reg_ex.append(c);
				// Read the escaped character
				i++;
				c = jasmine.charAt(token.getBegin() + i);
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
			decoded_options.append(jasmine.charAt(token.getBegin() + i));
		}

		return Value.createRegExpValue(decoded_reg_ex.toString(),
				decoded_options.toString());
	}

	private Value decodeSymbol(Token token) {
		return Value.createStringValue(token.getString(jasmine),
				StringType.UNQUOTED);
	}

	private Value decodeDictionaryKey(Token token) {
		switch (token.getType()) {
		case STRING_DOUBLE_QUOTED:
			return decodeStringDoubleQuoted(token);
		case STRING_SINGLE_QUOTED:
			return decodeStringSingleQuoted(token);
		case SYMBOL:
			return decodeSymbol(token);
		}

		Helpers.not_reached();
		return null;
	}

	private void parseSymbol(Token o_token) {
		char c = jasmine.charAt(position);
		if (is_jasmine_symbol_char(c)) {
			o_token.set(TokenType.SYMBOL, position, 0);

			do {
				o_token.incLength();
				c = o_token.nextChar(jasmine);

			} while (is_jasmine_symbol_char(c));
		}
	}

	private void parseNumberToken(Token o_token) {
		// We just grab the number here. We validate the size in DecodeNumber.
		// According to RFC4627, a valid number is: [minus] int [frac] [exp]
		o_token.set(TokenType.NUMBER, position, 0);

		char c = jasmine.charAt(position);
		if ('-' == c) {
			o_token.incLength();
			c = o_token.nextChar(jasmine);
		}

		if (!Helpers.read_int(o_token, jasmine, false)) {
			o_token.setType(TokenType.INVALID);
			return;
		}

		// Optional fraction part
		c = o_token.nextChar(jasmine);
		if ('.' == c) {
			o_token.incLength();
			if (!Helpers.read_int(o_token, jasmine, true)) {
				o_token.setType(TokenType.INVALID);
				return;
			}
			c = o_token.nextChar(jasmine);
		}

		// Optional exponent part
		if ('e' == c || 'E' == c) {
			o_token.incLength();
			c = o_token.nextChar(jasmine);
			if ('-' == c || '+' == c) {
				o_token.incLength();
				c = o_token.nextChar(jasmine);
			}

			if (!Helpers.read_int(o_token, jasmine, true)) {
				o_token.setType(TokenType.INVALID);
				return;
			}
		}
	}

	private Value decodeNumber(Token token) {
		try {
			int num_int = Helpers.string_to_int(token.getBegin(), jasmine,
					token.getLength());
			return Value.createIntegerValue(num_int);
		} catch (Exception e) {
			// ignore
		}
		try {
			double num_double = Helpers.string_to_double(token.getBegin(),
					jasmine, token.getLength());
			if (Helpers.is_finite(num_double)) {
				return Value.createRealValue(num_double);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void parseStringDoubleQuoted(Token o_token) {
		o_token.set(TokenType.STRING_DOUBLE_QUOTED, position, 1);
		char c = o_token.nextChar(jasmine);
		while ('\0' != c) {
			if ('\\' == c) {
				o_token.incLength();
				c = o_token.nextChar(jasmine);
				// Make sure the escaped char is valid.
				switch (c) {
				case 'x':
					if (!Helpers.read_hex_digits(o_token, jasmine, 2)) {
						throw new InvalidEscapeInStringException('x',
								findLinePos(position + o_token.getLength() - 2));
					}
					break;

				case 'u':
					if (!Helpers.read_hex_digits(o_token, jasmine, 4)) {
						throw new InvalidEscapeInStringException('u',
								findLinePos(position + o_token.getLength() - 2));
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
					throw new InvalidEscapeInStringException(c,
							findLinePos(position + o_token.getLength() - 2));
				}

			} else if ('"' == c) {
				o_token.incLength();
				return;
			}

			o_token.incLength();
			c = o_token.nextChar(jasmine);
		}
		o_token.setType(TokenType.INVALID);
	}

	private Value decodeStringDoubleQuoted(Token token) {
		StringBuilder decoded_str = new StringBuilder();

		for (int i = 1; i < token.getLength() - 1; ++i) {
			char c = jasmine.charAt(token.getBegin() + i);
			if ('\\' == c) {
				++i;
				c = jasmine.charAt(token.getBegin() + i);
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
					decoded_str.append((Helpers.hex_to_int(jasmine.charAt(token
							.getBegin() + i + 1)) << 4)
							+ Helpers.hex_to_int(jasmine.charAt(token
									.getBegin() + i + 2)));
					i += 2;
					break;

				case 'u':
					decoded_str.append((Helpers.hex_to_int(jasmine.charAt(token
							.getBegin() + i + 1)) << 12)
							+ (Helpers.hex_to_int(jasmine.charAt(token
									.getBegin() + i + 2)) << 8)
							+ (Helpers.hex_to_int(jasmine.charAt(token
									.getBegin() + i + 3)) << 4)
							+ Helpers.hex_to_int(jasmine.charAt(token
									.getBegin() + i + 4)));
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

	private void parseStringSingleQuoted(Token o_token) {
		o_token.set(TokenType.STRING_SINGLE_QUOTED, position, 1);
		char c = o_token.nextChar(jasmine);

		while ('\0' != c) {
			if ('\'' == c) {
				o_token.incLength();
				c = o_token.nextChar(jasmine);
				if ('\'' != c) {
					return;
				}
			}

			o_token.incLength();
			c = o_token.nextChar(jasmine);
		}
		o_token.setType(TokenType.INVALID);
	}

	private Value decodeStringSingleQuoted(Token token) {
		StringBuilder decoded_str = new StringBuilder();

		for (int i = 1; i < token.getLength() - 1; ++i) {
			char c = jasmine.charAt(token.getBegin() + i);
			if ('\'' == c) {
				++i;
				c = jasmine.charAt(token.getBegin() + i);
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

	private boolean nextStringMatch(String str) {
		for (int i = 0; i < str.length(); ++i) {
			if (position == jasmine.length())
				return false;

			if (jasmine.charAt(position + i) != str.charAt(i))
				return false;
		}

		return true;
	}

	private void eatWhitespaceAndComments() {
		while (position != jasmine.length()) {
			switch (jasmine.charAt(position)) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				++position;
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

	private boolean eatComment() {
		if ('/' != jasmine.charAt(position))
			return false;

		char next_char = jasmine.charAt(position + 1);
		if ('/' == next_char) {
			// Line comment, read until \n or \r
			position += 2;
			while (position != jasmine.length()) {
				switch (jasmine.charAt(position)) {
				case '\n':
				case '\r':
					++position;
					return true;
				default:
					++position;
				}
			}
		} else if ('*' == next_char) {
			// Block comment, read until */
			position += 2;
			while (position != jasmine.length()) {
				if ('*' == jasmine.charAt(position)
						&& '/' == jasmine.charAt(position + 1)) {
					position += 2;
					return true;
				}
				++position;
			}
		} else {
			return false;
		}

		return true;
	}

	// / Returns true if given character is a valid Jasmine symbol character.
	private boolean is_jasmine_symbol_char(char c) {
		return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| (c >= '0' && c <= '9') || char_is_in(c, '_', '-', '.'));
	}

	private boolean token_type_is_in(TokenType t, TokenType... options) {
		for (TokenType o : options) {
			if (t == o) {
				return true;
			}
		}
		return false;
	}

	private boolean char_is_in(char c, char... options) {
		for (char o : options) {
			if (c == o) {
				return true;
			}
		}
		return false;
	}

	private LinePos findLinePos(int end) {
		int pos = 0, line = 0, column = 0;
		// Figure out the line and column the error occurred at.
		for (; pos != end; ++pos) {
			if (pos == jasmine.length()) {
				Helpers.not_reached();
			}

			if (jasmine.charAt(pos) == '\n') {
				line++;
				column = 1;
			} else {
				column++;
			}
		}
		return new LinePos(line, column);
	}

	private LinePos findLinePos(Token token) {
		return findLinePos(token.getBegin());
	}

}