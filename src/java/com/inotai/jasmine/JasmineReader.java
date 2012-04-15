package com.inotai.jasmine;

import com.inotai.jasmine.reader.Helpers;
import com.inotai.jasmine.reader.LinePos;
import com.inotai.jasmine.reader.Token;
import com.inotai.jasmine.reader.TokenType;
import com.inotai.jasmine.reader.error.BadRootElementTypeException;
import com.inotai.jasmine.reader.error.InvalidDictionaryKeyException;
import com.inotai.jasmine.reader.error.InvalidEscapeInStringException;
import com.inotai.jasmine.reader.error.ParserError;
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

	public static final String NULL = "null";
	public static final String TRUE = "true";
	public static final String FALSE = "false";

	public static final TokenType[] DICTIONARY_KEY_TOKENS = new TokenType[] {
			TokenType.STRING_DOUBLE_QUOTED, TokenType.STRING_SINGLE_QUOTED,
			TokenType.SYMBOL };

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

	public static Value read(final CharSequence input) {
		return new JasmineReader().toValue(input, true);
	}

	private void checkStackDepth() {
		if (stackDepth > STACK_LIMIT) {
			throw new TooMuchNestingException(findLinePos(position));
		}
	}

	private Value buildValue(final Token token) {
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
			ListValue list = (ListValue) value;

			while (token.getType() != TokenType.ARRAY_END) {
				Value element = buildValue(token);
				if (element == null) {
					return null;
				}
				list.add(element);
				// After a list value, we expect a comma or the end of the list.
				parseToken(token);
				if (token.getType() == TokenType.LIST_SEPARATOR) {
					position += token.getLength();
					parseToken(token);
					if (token.getType() == TokenType.ARRAY_END) {
						// Trailing comma is OK, stop parsing the Array.
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

				if (!Helpers.isOneOfTokenTypes(token.getType(),
						DICTIONARY_KEY_TOKENS)) {
					throw new InvalidDictionaryKeyException(findLinePos(token));
				}

				// Get key value
				Value dictKeyValue = decodeDictionaryKey(token);
				if (dictKeyValue == null)
					return null;

				// Get key string
				String dictKey = dictKeyValue.getAsString();

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
				((DictionaryValue) value).add(dictKey, dict_value);

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

	public Value toValue(CharSequence text, boolean checkRoot) {
		jasmine = text;
		final Token token = new Token(jasmine);
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

		} else {
			throw new ParserError("No root value found.");
		}

	}

	private void parseToken(final Token token) {
		eatWhitespaceAndComments();

		token.set(TokenType.INVALID, 0, 0);

		if (position == jasmine.length()) {
			token.setType(TokenType.END_OF_INPUT);
			return;
		}

		switch (jasmine.charAt(position)) {
		case 'n':
			if (nextStringMatch(NULL))
				token.set(TokenType.NULL, position, 4);
			else
				parseSymbol(token);
			break;

		case 't':
			if (nextStringMatch(TRUE))
				token.set(TokenType.BOOL_TRUE, position, 4);
			else
				parseSymbol(token);
			break;

		case 'f':
			if (nextStringMatch(FALSE))
				token.set(TokenType.BOOL_FALSE, position, 5);
			else
				parseSymbol(token);
			break;

		case '[':
			token.set(TokenType.ARRAY_BEGIN, position, 1);
			break;

		case ']':
			token.set(TokenType.ARRAY_END, position, 1);
			break;

		case ',':
			token.set(TokenType.LIST_SEPARATOR, position, 1);
			break;

		case '{':
			token.set(TokenType.OBJECT_BEGIN, position, 1);
			break;

		case '}':
			token.set(TokenType.OBJECT_END, position, 1);
			break;

		case ':':
			token.set(TokenType.OBJECT_PAIR_SEPARATOR, position, 1);
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
			parseNumberToken(token);
			break;

		case '-':
			parseNumberToken(token);
			if (token.getType() == TokenType.INVALID)
				parseSymbol(token);
			break;

		case '"':
			parseStringDoubleQuoted(token);
			break;

		case '\'':
			parseStringSingleQuoted(token);
			break;

		case '/':
			parseRegExp(token);
			break;

		default:
			parseSymbol(token);
		}
	}

	private void parseRegExp(final Token token) {
		token.set(TokenType.REG_EXP, position, 1);
		char c = token.nextChar();
		while ('\0' != c) {
			if ('\\' == c) {
				token.incLength();
				c = token.nextChar();
			} else if ('/' == c) {
				token.incLength();
				break;
			}

			token.incLength();
			c = token.nextChar();
		}

		if ('/' != c) {
			throw new UnexpectedlyTerminatedException(findLinePos(token));
		}

		c = token.nextChar();
		while ('\0' != c && Helpers.isJasmineSymbolChar(c)) {
			token.incLength();
			c = token.nextChar();
		}
	}

	private Value decodeRegExp(final Token token) {
		StringBuffer decodedRegEx = new StringBuffer();
		int i = 1;
		for (; i < token.getLength() - 1; ++i) {
			char c = jasmine.charAt(token.getBegin() + i);

			if ('\\' == c) {
				// Not escaped
				decodedRegEx.append(c);
				// Read the escaped character
				i++;
				c = jasmine.charAt(token.getBegin() + i);
				decodedRegEx.append(c);
			} else if ('/' == c) {
				// End of regexp, options may continue
				break;
			} else {
				decodedRegEx.append(c);
			}
		}
		StringBuffer decodedOptions = new StringBuffer();
		i++; // Skip '/'
		for (; i < token.getLength(); ++i) {
			decodedOptions.append(jasmine.charAt(token.getBegin() + i));
		}
		return Value.createRegExpValue(decodedRegEx.toString(),
				decodedOptions.toString());
	}

	private Value decodeSymbol(final Token token) {
		return Value.createStringValue(token.getString(), StringType.UNQUOTED);
	}

	private Value decodeDictionaryKey(final Token token) {
		switch (token.getType()) {
		case STRING_DOUBLE_QUOTED:
			return decodeStringDoubleQuoted(token);
		case STRING_SINGLE_QUOTED:
			return decodeStringSingleQuoted(token);
		case SYMBOL:
			return decodeSymbol(token);
		}
		throw new ParserError(
				"Invalid dictionary key value found where I did not expect it.");
	}

	private void parseSymbol(final Token token) {
		char c = jasmine.charAt(position);
		if (Helpers.isJasmineSymbolChar(c)) {
			token.set(TokenType.SYMBOL, position, 0);

			do {
				token.incLength();
				c = token.nextChar();

			} while (Helpers.isJasmineSymbolChar(c));
		}
	}

	private void parseNumberToken(final Token token) {
		// We just grab the number here. We validate the size in DecodeNumber.
		// According to RFC4627, a valid number is: [minus] int [frac] [exp]
		token.set(TokenType.NUMBER, position, 0);

		char c = jasmine.charAt(position);
		if ('-' == c) {
			token.incLength();
			c = token.nextChar();
		}

		if (!Helpers.readInteger(token, false)) {
			token.setType(TokenType.INVALID);
			return;
		}

		// Optional fraction part
		c = token.nextChar();
		if ('.' == c) {
			token.incLength();
			if (!Helpers.readInteger(token, true)) {
				token.setType(TokenType.INVALID);
				return;
			}
			c = token.nextChar();
		}

		// Optional exponent part
		if ('e' == c || 'E' == c) {
			token.incLength();
			c = token.nextChar();
			if ('-' == c || '+' == c) {
				token.incLength();
				c = token.nextChar();
			}

			if (!Helpers.readInteger(token, true)) {
				token.setType(TokenType.INVALID);
				return;
			}
		}
	}

	private Value decodeNumber(final Token token) {
		try {
			int num_int = Helpers.stringToInteger(token.getBegin(), jasmine,
					token.getLength());
			return Value.createIntegerValue(num_int);
		} catch (Exception e) {
			// ignore
		}
		try {
			double num_double = Helpers.stringToDouble(token.getBegin(),
					jasmine, token.getLength());
			if (!Double.isInfinite(num_double)) {
				return Value.createRealValue(num_double);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void parseStringDoubleQuoted(final Token token) {
		token.set(TokenType.STRING_DOUBLE_QUOTED, position, 1);
		char c = token.nextChar();
		while ('\0' != c) {
			if ('\\' == c) {
				token.incLength();
				c = token.nextChar();
				// Make sure the escaped char is valid.
				switch (c) {
				case 'x':
					if (!Helpers.readHexDigits(token, jasmine, 2)) {
						throw new InvalidEscapeInStringException('x',
								findLinePos(position + token.getLength() - 2));
					}
					break;

				case 'u':
					if (!Helpers.readHexDigits(token, jasmine, 4)) {
						throw new InvalidEscapeInStringException('u',
								findLinePos(position + token.getLength() - 2));
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
							findLinePos(position + token.getLength() - 2));
				}

			} else if ('"' == c) {
				token.incLength();
				return;
			}

			token.incLength();
			c = token.nextChar();
		}
		token.setType(TokenType.INVALID);
	}

	private Value decodeStringDoubleQuoted(final Token token) {
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
					decoded_str.append((Helpers.hexToInt(jasmine.charAt(token
							.getBegin() + i + 1)) << 4)
							+ Helpers.hexToInt(jasmine.charAt(token.getBegin()
									+ i + 2)));
					i += 2;
					break;

				case 'u':
					decoded_str.append((Helpers.hexToInt(jasmine.charAt(token
							.getBegin() + i + 1)) << 12)
							+ (Helpers.hexToInt(jasmine.charAt(token.getBegin()
									+ i + 2)) << 8)
							+ (Helpers.hexToInt(jasmine.charAt(token.getBegin()
									+ i + 3)) << 4)
							+ Helpers.hexToInt(jasmine.charAt(token.getBegin()
									+ i + 4)));
					i += 4;
					break;

				default:
					throw new ParserError(
							"Invalid string found where I did not expect it.");
				}

			} else {
				// Not escaped
				decoded_str.append(c);
			}
		}
		return Value.createStringValue(decoded_str.toString());
	}

	private void parseStringSingleQuoted(final Token token) {
		token.set(TokenType.STRING_SINGLE_QUOTED, position, 1);
		char c = token.nextChar();

		while ('\0' != c) {
			if ('\'' == c) {
				token.incLength();
				c = token.nextChar();
				if ('\'' != c) {
					return;
				}
			}

			token.incLength();
			c = token.nextChar();
		}
		token.setType(TokenType.INVALID);
	}

	private Value decodeStringSingleQuoted(final Token token) {
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

	private boolean nextStringMatch(final String str) {
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

		char nextChar = jasmine.charAt(position + 1);
		if ('/' == nextChar) {
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
		} else if ('*' == nextChar) {
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

	private LinePos findLinePos(int end) {
		int pos = 0, line = 0, column = 0;
		// Figure out the line and column the error occurred at.
		for (; pos != end; ++pos) {
			if (pos == jasmine.length()) {
				throw new ParserError(
						"Ran out of space while looking for the origin of an error.");
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