package com.inotai.jasmine;

import java.util.Stack;

import com.inotai.jasmine.value.Value;

public class JasmineFormatter {
	JasmineFormatter() {
	}

	public void beforeValue(Stack<Value> stack, StringBuilder builder) {
		// Nothing here
	}

	public void afterValue(Stack<Value> stack, StringBuilder builder) {
		// Nothing here
	}

	public void beforeListChild(Stack<Value> stack, Value value, int index,
			int last, StringBuilder builder) {
		// Nothing here
	}

	public void afterListChild(Stack<Value> stack, Value value, int index,
			int last, StringBuilder builder) {
		if (index < last) {
			builder.append(' ');
		}
	}

	public void beforeDictionaryChild(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		// Nothing here
	}

	public void afterDictionaryChild(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		if (index < last) {
			builder.append(' ');
		}
	}

	public void beforeDictionaryKey(Stack<Value> stack, Value value,
			String key, int index, int last, StringBuilder builder) {
		// Nothing here
	}

	public void afterDictionaryKey(Stack<Value> stack, Value value, String key,
			int index, int last, StringBuilder builder) {
		// Nothing here
	}
}