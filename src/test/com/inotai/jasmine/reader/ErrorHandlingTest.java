package com.inotai.jasmine.reader;

import junit.framework.TestCase;

import org.junit.Test;

import com.inotai.jasmine.JasmineReader;
import com.inotai.jasmine.reader.error.BadRootElementTypeException;
import com.inotai.jasmine.reader.error.TooMuchNestingException;
import com.inotai.jasmine.reader.error.UnexpectedDataAfterRootException;

public class ErrorHandlingTest extends TestCase {

	@Test
	public void testBooleanRoot() {
		try {
			JasmineReader.read("true");
			fail("Expected BadRootElementTypeException but none was thrown.");
		} catch (BadRootElementTypeException e) {
			// success
		}
	}

	@Test
	public void testNumberRoot() {
		try {
			JasmineReader.read("1234");
			fail("Expected BadRootElementTypeException but none was thrown.");
		} catch (BadRootElementTypeException e) {
			// success
		}
	}

	@Test
	public void testRegExpRoot() {
		try {
			JasmineReader.read("/reg exp/");
			fail("Expected BadRootElementTypeException but none was thrown.");
		} catch (BadRootElementTypeException e) {
			// success
		}

	}

	@Test
	public void testStringRoot() {
		try {
			JasmineReader.read("\"true\"");
			fail("Expected BadRootElementTypeException but none was thrown.");
		} catch (BadRootElementTypeException e) {
			// success
		}

	}

	@Test
	public void testNullRoot() {
		try {
			JasmineReader.read("null");
			fail("Expected BadRootElementTypeException but none was thrown.");
		} catch (BadRootElementTypeException e) {
			// success
		}
	}

	@Test
	public void testDataAfterObjectRoot() {
		try {
			JasmineReader.read("{}\n{}");
			fail("Expected UnexpectedDataAfterRootException but none was thrown.");
		} catch (UnexpectedDataAfterRootException e) {
			// success
		}
	}

	@Test
	public void testDataAfterListRoot() {
		try {
			JasmineReader.read("[]\n{}");
			fail("Expected UnexpectedDataAfterRootException but none was thrown.");
		} catch (UnexpectedDataAfterRootException e) {
			// success
		}
	}

	@Test
	public void testTooMuchNesting() {
		String val = "null";
		for (int i = 0; i < 101; i++) {
			val = "{ value: " + val + "}";
		}
		try {
			JasmineReader.read(val);
			fail("Expected TooMuchNestingException but none was thrown.");
		} catch (TooMuchNestingException e) {
			// success
		}
	}

}
