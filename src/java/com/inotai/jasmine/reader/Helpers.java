package com.inotai.jasmine.reader;

public class Helpers {

	public static void not_reached() {
		throw new IllegalStateException("Unexpected state");
	}

	public static int hex_to_int(char c) {
		if ('0' <= c && c <= '9')
			return c - '0';
		else if ('A' <= c && c <= 'F')
			return c - 'A' + 10;
		else if ('a' <= c && c <= 'f')
			return c - 'a' + 10;

		not_reached();
		return 0;
	}

	// ------------------------------------------------------------------------
	// A helper method for ParseNumberToken. It reads an int from the end of
	// token. The method returns false if there is no valid integer at the end
	// of
	// the token.
	public static boolean read_int(Token token, CharSequence input,
			boolean can_have_leading_zeros)
	// ------------------------------------------------------------------------
	{
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

		if (!can_have_leading_zeros && len > 1 && '0' == first)
			return false;

		return true;
	}

	// ------------------------------------------------------------------------
	// A helper method for ParseStringToken. It reads |digits| hex digits from
	// the
	// token. If the sequence if digits is not valid (contains other
	// characters),
	// the method returns false.
	public static boolean read_hex_digits(Token token, CharSequence input, int digits)
	// ------------------------------------------------------------------------
	{
		for (int i = 1; i <= digits; ++i) {
			char c = input.charAt(token.getBegin() + token.getLength() + i);
			if ('\0' == c)
				return false;
			if (!(('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F')))
				return false;
		}

		token.incLength(digits);
		return true;
	}


	// TODO search the string for numeric chars, until a non-numeric char is found and return parsed int
	public static int string_to_int( int startPos, CharSequence intpu, int length, int base)
	//------------------------------------------------------------------------
	{
		/*INO_NOT_NULL( o_int );
		errno = 0;
		C16 * input_end = NULL;
		long r = wcstol( i_string, &input_end, base );
		*o_int = static_cast<xint>(r);
		return valid;*/
		return 0;
	}
	
	// TODO same as _to_int
	public static double string_to_double(int startPos, CharSequence input, int length) {
		return 0;
	}
	
	public static boolean is_finite( double number )
	//------------------------------------------------------------------------
	{
		return number < Double.POSITIVE_INFINITY && number < Double.NEGATIVE_INFINITY;
	}

}
