package com.inotai.jasmine;

import org.junit.Test;

import com.inotai.jasmine.value.DictionaryValue;

import static org.junit.Assert.*;

public class JasmineReaderTest {

	@Test
	public void testSimpleDisctionary() {
		String in = "{key: 'value'}";
		DictionaryValue val= (DictionaryValue) JasmineReader.read(in);
		assertNotNull(val);
		assertTrue(val.has("key"));
		assertEquals("value", val.get("key").getAsString());
	}

}
