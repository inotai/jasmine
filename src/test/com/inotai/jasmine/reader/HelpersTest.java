package com.inotai.jasmine.reader;

import junit.framework.TestCase;

import org.junit.Test;

public class HelpersTest extends TestCase {

	@Test
	public void testParseIntString() {
		assertEquals(764567, Helpers.stringToInteger(0, "764567", 6));
	}

	@Test
	public void testParseIntStringWithWhiteSpace() {
		assertEquals(764567, Helpers.stringToInteger(0, "   764567", 9));
	}

	@Test
	public void testParseIntStringNegative() {
		assertEquals(-764567, Helpers.stringToInteger(0, "-764567", 7));
	}

	@Test
	public void testParseIntStringNegativeWithWhitespace() {
		assertEquals(-764567, Helpers.stringToInteger(0, "      -764567", 13));
	}

	@Test
	public void testParseIntStringNegativeWithWhitespaceAndShift() {
		assertEquals(-764567,
				Helpers.stringToInteger(10, "123456789  -764567", 8));
	}

	@Test
	public void testParseDoubleString() {
		assertEquals(764567.123, Helpers.stringToDouble(0, "764567.123", 10),
				0);
	}

	@Test
	public void testParseDoubleStringWithWhiteSpace() {
		assertEquals(764567.123,
				Helpers.stringToDouble(0, "   764567.123", 13), 0);
	}

	@Test
	public void testParseDoubleStringNegative() {
		assertEquals(-764567.123,
				Helpers.stringToDouble(0, "-764567.123", 11), 0);
	}

	@Test
	public void testParseDoubleStringNegativeWithWhitespace() {
		assertEquals(-764567.123,
				Helpers.stringToDouble(0, "      -764567.123", 17), 0);
	}

	@Test
	public void testParseDoubleStringNegativeWithWhitespaceAndShift() {
		assertEquals(-764567.123,
				Helpers.stringToDouble(10, "123456789  -764567.123", 12), 0);
	}

}
