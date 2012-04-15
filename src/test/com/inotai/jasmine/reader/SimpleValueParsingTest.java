package com.inotai.jasmine.reader;

import junit.framework.TestCase;

import org.junit.Test;

import com.inotai.jasmine.JasmineReader;
import com.inotai.jasmine.reader.error.InvalidEscapeInStringException;
import com.inotai.jasmine.reader.error.UnexpectedTokenException;
import com.inotai.jasmine.reader.error.UnexpectedlyTerminatedException;
import com.inotai.jasmine.value.BooleanValue;
import com.inotai.jasmine.value.IntegerValue;
import com.inotai.jasmine.value.RealValue;
import com.inotai.jasmine.value.RegExpValue;
import com.inotai.jasmine.value.StringType;
import com.inotai.jasmine.value.StringValue;
import com.inotai.jasmine.value.Value;
import com.inotai.jasmine.value.ValueType;

public class SimpleValueParsingTest extends TestCase {

	@Test
	public void testBoolean() {
		Value val = new JasmineReader().toValue("true", false);
		assertNotNull(val);
		assertTrue(val instanceof BooleanValue);
		assertEquals(true, val.getAsBoolean());
	}

	@Test
	public void testPositiveInteger() {
		Value val = new JasmineReader().toValue("12345", false);
		assertNotNull(val);
		assertTrue(val instanceof IntegerValue);
		assertEquals(12345, val.getAsInteger());
	}

	@Test
	public void testNegativeInteger() {
		Value val = new JasmineReader().toValue("-12345", false);
		assertNotNull(val);
		assertTrue(val instanceof IntegerValue);
		assertEquals(-12345, val.getAsInteger());
	}

	@Test
	public void testPositiveDouble() {
		Value val = new JasmineReader().toValue("12345.123", false);
		assertNotNull(val);
		assertTrue(val instanceof RealValue);
		assertEquals(12345.123, val.getAsReal(), 0);
	}

	@Test
	public void testNegativeReal() {
		Value val = new JasmineReader().toValue("-12345.123", false);
		assertNotNull(val);
		assertTrue(val instanceof RealValue);
		assertEquals(-12345.123, val.getAsReal(), 0);
	}

	@Test
	public void testPositiveExpDouble() {
		Value val = new JasmineReader().toValue("12345.123e3", false);
		assertNotNull(val);
		assertTrue(val instanceof RealValue);
		assertEquals(12345.123e3, val.getAsReal(), 0);
	}

	@Test
	public void testPositiveExpWithPlusDouble() {
		Value val = new JasmineReader().toValue("12345.123e+3", false);
		assertNotNull(val);
		assertTrue(val instanceof RealValue);
		assertEquals(12345.123e+3, val.getAsReal(), 0);
	}

	@Test
	public void testNegativeExpDouble() {
		Value val = new JasmineReader().toValue("12345.123e-3", false);
		assertNotNull(val);
		assertTrue(val instanceof RealValue);
		assertEquals(12345.123e-3, val.getAsReal(), 0);
	}

	@Test
	public void testRegEx() {
		Value val = new JasmineReader().toValue("/abcdef\\/\\/&'\"/", false);
		assertNotNull(val);
		assertTrue(val instanceof RegExpValue);
		RegExpValue re = (RegExpValue) val;
		assertEquals(re.getRegExp(), "abcdef\\/\\/&'\"");
		assertEquals(re.getRegExpOptions(), "");
	}

	@Test
	public void testRegExWithOps() {
		Value val = new JasmineReader()
				.toValue("/abcdef\\/\\/&'\"/opts", false);
		assertNotNull(val);
		assertTrue(val instanceof RegExpValue);
		RegExpValue re = (RegExpValue) val;
		assertEquals(re.getRegExp(), "abcdef\\/\\/&'\"");
		assertEquals(re.getRegExpOptions(), "opts");
	}

	@Test
	public void testRegExUnterminated() {
		try {
			new JasmineReader().toValue("/a", false);
			fail("Expected UnexpectedlyTerminatedException but none was thrown.");
		} catch (UnexpectedlyTerminatedException e) {
			// success
		}
	}

	@Test
	public void testStringDoubleQuoted() {
		Value val = new JasmineReader().toValue("\"test string\"", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("test string", str.getAsString());
		assertEquals(StringType.DOUBLE_QUOTED, str.getStringType());
	}

	@Test
	public void testStringDoubleQuotedEscapes() {
		Value val = new JasmineReader().toValue(
				"\"\\\"\\/\\b\\f\\n\\r\\t\\\\\"", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("\"/\b\f\n\r\t\\", str.getAsString());
		assertEquals(StringType.DOUBLE_QUOTED, str.getStringType());
	}

	@Test
	public void testStringDoubleQuotedEscapesHex() {
		Value val = new JasmineReader().toValue("\"\\xAB\\uABCD\"", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("17143981", str.getAsString());
		assertEquals(StringType.DOUBLE_QUOTED, str.getStringType());
	}
	
	@Test
	public void testStringDoubleQuotedEscapesInvalidXHex() {
		try {
			new JasmineReader().toValue("\"\\xA\"", false);
			fail("Expected InvalidEscapeInStringException but none was thrown.");
		} catch (InvalidEscapeInStringException e) {
			// success
		}
	}

	@Test
	public void testStringDoubleQuotedEscapesInvalidUHex() {
		try {
			new JasmineReader().toValue("\"\\uA\"", false);
			fail("Expected InvalidEscapeInStringException but none was thrown.");
		} catch (InvalidEscapeInStringException e) {
			// success
		}
	}

	@Test
	public void testStringDoubleQuotedInvalidEscape() {
		try {
			new JasmineReader().toValue("\"\\a\"", false);
			fail("Expected InvalidEscapeInStringException but none was thrown.");
		} catch (InvalidEscapeInStringException e) {
			// success
		}
	}
	
	@Test
	public void testStringDoubleQuotedUnclosed() {
		try {
			new JasmineReader().toValue("\"abdc", false);
			fail("Expected UnexpectedTokenException but none was thrown.");
		} catch (UnexpectedTokenException e) {
			// success
		}
	}

	@Test
	public void testStringSingleQuoted() {
		Value val = new JasmineReader().toValue("'test string'", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("test string", str.getAsString());
		assertEquals(StringType.SINGLE_QUOTED, str.getStringType());
	}

	@Test
	public void testStringSingleQuotedEscapes() {
		Value val = new JasmineReader().toValue("'test\\string'", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("test\\string", str.getAsString());
		assertEquals(StringType.SINGLE_QUOTED, str.getStringType());
	}

	@Test
	public void testStringSingleQuotedUnclosed() {
		try {
			new JasmineReader().toValue("'abdc", false);
			fail("Expected UnexpectedTokenException but none was thrown.");
		} catch (UnexpectedTokenException e) {
			// success
		}
	}
	
	@Test
	public void testStringUnquoted() {
		Value val = new JasmineReader().toValue("test_string", false);
		assertNotNull(val);
		assertTrue(val instanceof StringValue);
		StringValue str = (StringValue) val;
		assertEquals("test_string", str.getAsString());
		assertEquals(StringType.UNQUOTED, str.getStringType());
	}

	@Test
	public void testNullValue() {
		Value val = new JasmineReader().toValue("null", false);
		assertNotNull(val);
		assertEquals(ValueType.NULL, val.getType());
	}

}
