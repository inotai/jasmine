package com.inotai.jasmine.reader;

import org.junit.Test;
import static org.junit.Assert.*;

public class HelpersTest {
	
	@Test
	public void testParseIntString() {
		assertEquals(764567, Helpers.string_to_int(0, "764567", 6));
	}

	@Test
	public void testParseIntStringWithWhiteSpace() {
		assertEquals(764567, Helpers.string_to_int(0, "   764567", 9));
	}

	@Test
	public void testParseIntStringNegative() {
		assertEquals(-764567, Helpers.string_to_int(0, "-764567", 7));
	}
	
	@Test
	public void testParseIntStringNegativeWithWhitespace() {
		assertEquals(-764567, Helpers.string_to_int(0, "      -764567", 13));
	}

	@Test
	public void testParseIntStringNegativeWithWhitespaceAndShift() {
		assertEquals(-764567, Helpers.string_to_int(10, "123456789  -764567", 8));
	}
	@Test
	public void testParseDoubleString() {
		assertEquals(764567.123, Helpers.string_to_double(0, "764567.123", 10), 0);
	}

	@Test
	public void testParseDoubleStringWithWhiteSpace() {
		assertEquals(764567.123, Helpers.string_to_double(0, "   764567.123", 13), 0);
	}

	@Test
	public void testParseDoubleStringNegative() {
		assertEquals(-764567.123, Helpers.string_to_double(0, "-764567.123", 11), 0);
	}
	
	@Test
	public void testParseDoubleStringNegativeWithWhitespace() {
		assertEquals(-764567.123, Helpers.string_to_double(0, "      -764567.123", 17), 0);
	}

	@Test
	public void testParseDoubleStringNegativeWithWhitespaceAndShift() {
		assertEquals(-764567.123, Helpers.string_to_double(10, "123456789  -764567.123", 12), 0);
	}


}
