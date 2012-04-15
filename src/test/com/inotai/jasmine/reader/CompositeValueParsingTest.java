package com.inotai.jasmine.reader;

import junit.framework.TestCase;

import org.junit.Test;

import com.inotai.jasmine.JasmineReader;
import com.inotai.jasmine.reader.error.InvalidDictionaryKeyException;
import com.inotai.jasmine.value.DictionaryValue;
import com.inotai.jasmine.value.ListValue;
import com.inotai.jasmine.value.Value;

public class CompositeValueParsingTest extends TestCase {

	@Test
	public void testDictionary() {
		Value val = new JasmineReader().toValue(
				"{key: \"value\", \"other-key\": \"other-value\"}", false);
		assertNotNull(val);
		assertTrue(val instanceof DictionaryValue);
		DictionaryValue dict = (DictionaryValue) val;
		assertTrue(dict.has("key"));
		assertEquals("value", dict.get("key").getAsString());
		assertTrue(dict.has("other-key"));
		assertEquals("other-value", dict.get("other-key").getAsString());
	}

	@Test
	public void testDictionaryNoComas() {
		Value val = new JasmineReader().toValue(
				"{key: \"value\"\n\"other-key\": \"other-value\"}", false);
		assertNotNull(val);
		assertTrue(val instanceof DictionaryValue);
		DictionaryValue dict = (DictionaryValue) val;
		assertTrue(dict.has("key"));
		assertEquals("value", dict.get("key").getAsString());
		assertTrue(dict.has("other-key"));
		assertEquals("other-value", dict.get("other-key").getAsString());
	}

	@Test
	public void testBooleanDictionaryKey() {
		try {
			JasmineReader.read("{ true: 'test'}");
			fail("Expected InvalidDictionaryKeyException but none was thrown.");
		} catch (InvalidDictionaryKeyException e) {
			// success
		}
	}

	@Test
	public void testRegExpDictionaryKey() {
		try {
			JasmineReader.read("{ /aaaa/: 'test'}");
			fail("Expected InvalidDictionaryKeyException but none was thrown.");
		} catch (InvalidDictionaryKeyException e) {
			// success
		}
	}

	@Test
	public void testNullDictionaryKey() {
		try {
			JasmineReader.read("{ null: 'test'}");
			fail("Expected InvalidDictionaryKeyException but none was thrown.");
		} catch (InvalidDictionaryKeyException e) {
			// success
		}
	}

	@Test
	public void testArray() {
		Value val = new JasmineReader().toValue("[\"one\", \"two\"]", false);
		assertNotNull(val);
		assertTrue(val instanceof ListValue);
		ListValue list = (ListValue) val;
		assertEquals(2, list.size());
		assertEquals("one", list.get(0).getAsString());
		assertEquals("two", list.get(1).getAsString());
	}

	@Test
	public void testArrayNoComas() {
		Value val = new JasmineReader().toValue("[\"one\"\n\"two\"]", false);
		assertNotNull(val);
		assertTrue(val instanceof ListValue);
		ListValue list = (ListValue) val;
		assertEquals(2, list.size());
		assertEquals("one", list.get(0).getAsString());
		assertEquals("two", list.get(1).getAsString());
	}

}
